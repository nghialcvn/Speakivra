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
 const g=guest();await assertFails(claim(env.unauthenticatedContext().firestore(),'A',g));await assertFails(claim(db('A',false),'A',g));
 await assertFails(claim(db('A'),'A',g,'0'.repeat(64)));await assertSucceeds(claim(db('A'),'A',g));
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
 for(const mutation of [{name:5},{name:''},{name:'x'.repeat(31)},{avatar:-1},{avatar:6},{profileId:'f'.repeat(64)},{admin:true},{progressJson:'x'.repeat(131073)},{createdAt:Timestamp.fromMillis(1)},{revision:9000000001}])
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
