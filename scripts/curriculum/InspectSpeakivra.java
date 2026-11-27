package com.nghialcvn.speakivra;
import org.json.*;
import java.nio.file.*;
public class InspectSpeakivra {
  static JSONObject phrase(Phrase p){return new JSONObject().put("id",p.getId()).put("en",p.getEnglish()).put("vi",p.getVietnamese()).put("tip",p.getTip());}
  public static void main(String[] args)throws Exception{
    JSONObject root=new JSONObject(); JSONArray lessons=new JSONArray(),readings=new JSONArray(),exercises=new JSONArray();
    for(Lesson l: Lessons.INSTANCE.getCatalogue()){
      JSONArray ps=new JSONArray(),ts=new JSONArray();
      for(Phrase p:l.getPhrases())ps.put(phrase(p));
      for(ConversationTurn t:l.getConversation())ts.put(new JSONObject().put("id",t.getId()).put("prompt",t.getPrompt()).put("promptVi",t.getTranslation()).put("reply",t.getReply()).put("replyVi",t.getReplyTranslation()));
      lessons.put(new JSONObject().put("id",l.getId()).put("level",Lessons.INSTANCE.levelOf(l)).put("title",l.getTitle()).put("vi",l.getVietnamese()).put("phrases",ps).put("turns",ts));
    }
    for(ReadingPassage p:ExtraPracticeData.INSTANCE.getPassages()){
      JSONArray ss=new JSONArray(),vs=new JSONArray();
      for(ReadingSentence s:p.getSentences())ss.put(new JSONObject().put("id",s.getId()).put("en",s.getEnglish()).put("vi",s.getVietnamese()));
      for(PracticeVocabulary v:p.getVocabulary())vs.put(new JSONObject().put("en",v.getEnglish()).put("vi",v.getVietnamese()));
      readings.put(new JSONObject().put("id",p.getId()).put("level",p.getLevel()).put("lessonId",p.getLessonId()).put("title",p.getTitle()).put("vi",p.getVietnamese()).put("topic",p.getTopic()).put("sentences",ss).put("vocabulary",vs));
    }
    java.util.List<SpeakingExercise> all=new java.util.ArrayList<>(ExtraPracticeData.INSTANCE.getExercises());all.addAll(HotelCurriculum.INSTANCE.getExercises());
    for(SpeakingExercise e:all){JSONArray qs=new JSONArray();for(ExerciseQuestion q:e.getQuestions())qs.put(new JSONObject().put("id",q.getId()).put("vi",q.getVietnamese()).put("answers",q.getAnswers()));exercises.put(new JSONObject().put("id",e.getId()).put("level",e.getLevel()).put("lessonId",e.getLessonId()).put("topic",e.getTopic()).put("questions",qs));}
    root.put("lessons",lessons).put("readings",readings).put("exercises",exercises);
    Files.writeString(Path.of(args[0]),root.toString());
    for(String level:new String[]{"A1","A2","B1","B2","C1","C2","FO","HK"}){
      int n=0,p=0,t=0,r=0,rs=0,e=0,eq=0;
      for(int i=0;i<lessons.length();i++){JSONObject l=lessons.getJSONObject(i);if(l.getString("level").equals(level)){n++;p+=l.getJSONArray("phrases").length();t+=l.getJSONArray("turns").length();}}
      for(int i=0;i<readings.length();i++){JSONObject l=readings.getJSONObject(i);if(l.getString("level").equals(level)){r++;rs+=l.getJSONArray("sentences").length();}}
      for(int i=0;i<exercises.length();i++){JSONObject l=exercises.getJSONObject(i);String group=l.getString("level");for(int j=0;j<lessons.length();j++){JSONObject lesson=lessons.getJSONObject(j);if(lesson.getString("id").equals(l.getString("lessonId"))){group=lesson.getString("level");break;}}if(group.equals(level)){e++;eq+=l.getJSONArray("questions").length();}}
      System.out.printf("%s: %d topics, %d phrases, %d turns, %d readings/%d sentences, %d exercises/%d questions%n",level,n,p,t,r,rs,e,eq);
    }
  }
}
