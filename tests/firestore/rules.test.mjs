import {readFileSync} from 'node:fs';
import {createHash, randomBytes} from 'node:crypto';
import {after, before, beforeEach, test} from 'node:test';
import assert from 'node:assert/strict';
import {initializeTestEnvironment, assertFails, assertSucceeds} from '@firebase/rules-unit-testing';
import {doc,setDoc,getDoc,getDocs,collection,query,limit,deleteDoc,writeBatch,serverTimestamp,updateDoc,Timestamp} from 'firebase/firestore';
let env;
before(async()=>{env=await initializeTestEnvironment({projectId:'demo-speakivra-spark',firestore:{rules:readFileSync('../../firestore.rules','utf8')}});});
beforeEach(async()=>env.clearFirestore());
after(async()=>env.cleanup());
const db=(uid,verified=true)=>env.authenticatedContext(uid,{email_verified:verified}).firestore();
const guest=()=>{const secret=randomBytes(32).toString('hex');return {secret,id:createHash('sha256').update(secret).digest('hex')};};
const claimRef=(d,g)=>doc(d,'claims',g.id);
const proofRef=(d,uid,g)=>doc(d,'accounts',uid,'claimProofs',g.id);
const profileRef=(d,uid,g)=>doc(d,'accounts',uid,'profiles',g.id);
async function claim(d,uid,g,secret=g.secret){const b=writeBatch(d);b.set(claimRef(d,g),{schema:2,ownerUid:uid,createdAt:serverTimestamp()});b.set(proofRef(d,uid,g),{secret,createdAt:serverTimestamp()});return b.commit();}
const snapshot=g=>({schema:1,profileId:g.id,name:'Nghĩa',avatar:0,progressJson:'{}',revision:1,createdAt:serverTimestamp(),updatedAt:serverTimestamp()});
test('claim requires verified auth and actual secret, not displayed serial',async()=>{
 const g=guest();await assertSucceeds(getDoc(claimRef(db('A'),g)));await assertFails(claim(env.unauthenticatedContext().firestore(),'A',g));await assertFails(claim(db('A',false),'A',g));
 await assertFails(claim(db('A'),'A',g,'0'.repeat(64)));await assertFails(claim(db('A'),'A',g,g.id.slice(0,20)));await assertSucceeds(claim(db('A'),'A',g));
});
test('claim and proof must be atomic; no foreign or even owner proof reads',async()=>{
 const g=guest(),a=db('A');await assertFails(setDoc(claimRef(a,g),{schema:2,ownerUid:'A',createdAt:serverTimestamp()}));
 await assertFails(setDoc(proofRef(a,'A',g),{secret:g.secret,createdAt:serverTimestamp()}));await assertSucceeds(claim(a,'A',g));
 await assertFails(getDoc(proofRef(a,'A',g)));await assertFails(getDoc(proofRef(db('B'),'A',g)));
 await assertSucceeds(deleteDoc(proofRef(a,'A',g)));assert.equal((await getDoc(claimRef(a,g))).data().ownerUid,'A');
});
test('claimed identity cannot be reassigned, replayed, listed or deleted, even by owner',async()=>{
 const g=guest(),a=db('A');await claim(a,'A',g);await assertFails(claim(db('B'),'B',g));await assertFails(claim(a,'A',g));
 await assertFails(updateDoc(claimRef(a,g),{ownerUid:'B'}));await assertFails(deleteDoc(claimRef(a,g)));
 await assertFails(getDoc(claimRef(db('B'),g)));await assertFails(getDocs(collection(a,'claims')));
});
test('concurrent claims admit exactly one owner; owner can read idempotent receipt',async()=>{
 const g=guest(),r=await Promise.allSettled([claim(db('A'),'A',g),claim(db('B'),'B',g)]);
 assert.equal(r.filter(v=>v.status==='fulfilled').length,1);const uid=r[0].status==='fulfilled'?'A':'B';
 assert.equal((await getDoc(claimRef(db(uid),g))).data().ownerUid,uid);
});
test('caller cannot create another owner or privileged extra fields',async()=>{
 const g=guest(),a=db('A');await assertFails(claim(a,'B',g));const b=writeBatch(a);
 b.set(claimRef(a,g),{schema:2,ownerUid:'A',createdAt:serverTimestamp(),admin:true});b.set(proofRef(a,'A',g),{secret:g.secret,createdAt:serverTimestamp()});await assertFails(b.commit());
});
test('snapshots need a claim and validate all required fields on create AND update',async()=>{
 const g=guest(),a=db('A'),ref=profileRef(a,'A',g);await assertFails(setDoc(ref,snapshot(g)));await claim(a,'A',g);
 await assertSucceeds(setDoc(ref,snapshot(g)));await assertSucceeds(updateDoc(ref,{name:'Nghĩa An',revision:2,updatedAt:serverTimestamp()}));
 for(const mutation of [{schema:1.5},{schema:'1'},{name:5},{name:''},{name:'x'.repeat(31)},{avatar:-1},{avatar:6},{profileId:'f'.repeat(64)},{admin:true},{progressJson:'x'.repeat(524289)},{createdAt:Timestamp.fromMillis(1)},{revision:9000000001}])
  await assertFails(updateDoc(ref,{revision:3,updatedAt:serverTimestamp(),...mutation}));
 await assertFails(setDoc(ref,{name:'missing fields'}));await assertFails(updateDoc(ref,{name:'No revision increment',updatedAt:serverTimestamp()}));
});
test('account scopes, bounded lists and erasure never unlock a serial',async()=>{
 const g=guest(),a=db('A');await claim(a,'A',g);await setDoc(profileRef(a,'A',g),snapshot(g));
 await assertFails(getDoc(profileRef(db('B'),'A',g)));await assertFails(setDoc(profileRef(db('B'),'A',g),snapshot(g)));
 await assertFails(getDocs(collection(a,'accounts','A','profiles')));await assertSucceeds(getDocs(query(collection(a,'accounts','A','profiles'),limit(200))));
 await assertFails(getDocs(query(collection(db('B'),'accounts','A','profiles'),limit(200))));
 await assertSucceeds(deleteDoc(profileRef(a,'A',g)));await assertFails(claim(db('B'),'B',g));
});

const levels=['A1','A2','B1','B2','C1','C2','FO','HK'];
const languageRef=(d,uid,g,level='A1')=>doc(d,'accounts',uid,'profiles',g.id,'languageProgress',`zh_${level}`);
const languageSnapshot=(g,level='A1',progressJson='{}')=>({schema:1,profileId:g.id,language:'zh',level,progressJson,revision:1,createdAt:serverTimestamp(),updatedAt:serverTimestamp()});
test('all eight private levels and the profile can commit atomically within rule access limits',async()=>{
 const g=guest(),a=db('A');await claim(a,'A',g);const b=writeBatch(a);
 b.set(profileRef(a,'A',g),snapshot(g));
 for(const level of levels)b.set(languageRef(a,'A',g,level),languageSnapshot(g,level,'x'.repeat(131072)));
 await assertSucceeds(b.commit());
 for(const level of levels)assert.equal((await getDoc(languageRef(a,'A',g,level))).data().progressJson.length,131072);
 await assertFails(getDocs(collection(a,'accounts','A','profiles',g.id,'languageProgress')));
});
test('language data needs the immutable claim and a parent, including exact missing reads',async()=>{
 const g=guest(),a=db('A'),ref=languageRef(a,'A',g);await assertFails(getDoc(ref));await assertFails(setDoc(ref,languageSnapshot(g)));
 await claim(a,'A',g);await assertSucceeds(getDoc(ref));await assertFails(setDoc(ref,languageSnapshot(g)));
 await setDoc(profileRef(a,'A',g),snapshot(g));await assertSucceeds(setDoc(ref,languageSnapshot(g)));
 for(const d of [db('B'),db('A',false),env.unauthenticatedContext().firestore()]){
  const foreign=languageRef(d,'A',g);await assertFails(getDoc(foreign));await assertFails(setDoc(foreign,languageSnapshot(g)));await assertFails(deleteDoc(foreign));
 }
 const other=guest();await claim(db('B'),'B',other);await assertFails(setDoc(languageRef(a,'A',other),languageSnapshot(other)));
});
test('language schema validates path, bytes, revision and immutable timestamps',async()=>{
 const g=guest(),a=db('A'),ref=languageRef(a,'A',g);await claim(a,'A',g);await setDoc(profileRef(a,'A',g),snapshot(g));
 await setDoc(ref,languageSnapshot(g));await assertSucceeds(updateDoc(ref,{revision:2,progressJson:'learned',updatedAt:serverTimestamp()}));
 for(const mutation of [{schema:2},{schema:1.1},{language:'en'},{level:'C2'},{profileId:'f'.repeat(64)},{admin:true},{progressJson:[]},{progressJson:'x'.repeat(131073)},{progressJson:'中'.repeat(50000)},{createdAt:Timestamp.fromMillis(1)},{revision:1000000001}])
  await assertFails(updateDoc(ref,{revision:3,updatedAt:serverTimestamp(),...mutation}));
 await assertFails(updateDoc(ref,{progressJson:'no revision',updatedAt:serverTimestamp()}));
 await assertFails(setDoc(languageRef(a,'A',g,'A3'),languageSnapshot(g,'A3')));
 await assertFails(setDoc(languageRef(a,'A',g,'A2'),{profileId:g.id}));
});
test('a rejected level aborts root migration and every other level in the same commit',async()=>{
 const g=guest(),a=db('A'),root=profileRef(a,'A',g);await claim(a,'A',g);
 await setDoc(root,{...snapshot(g),progressJson:'legacy-Chinese-progress'});
 const b=writeBatch(a);b.update(root,{progressJson:'compatibility-only',revision:2,updatedAt:serverTimestamp()});
 b.set(languageRef(a,'A',g,'A1'),languageSnapshot(g));
 b.set(languageRef(a,'A',g,'A2'),languageSnapshot(g,'A2','x'.repeat(131073)));
 await assertFails(b.commit());assert.equal((await getDoc(root)).data().progressJson,'legacy-Chinese-progress');
 assert.equal((await getDoc(root)).data().revision,1);assert.equal((await getDoc(languageRef(a,'A',g))).exists(),false);
});
test('a later old-client root update cannot overwrite migrated level documents',async()=>{
 const g=guest(),a=db('A'),root=profileRef(a,'A',g);await claim(a,'A',g);await setDoc(root,snapshot(g));
 const ref=languageRef(a,'A',g);await setDoc(ref,languageSnapshot(g,'A1','new-progress'));
 await updateDoc(root,{progressJson:'old-client-progress',revision:2,updatedAt:serverTimestamp()});
 assert.equal((await getDoc(ref)).data().progressJson,'new-progress');await assertSucceeds(deleteDoc(ref));
});
