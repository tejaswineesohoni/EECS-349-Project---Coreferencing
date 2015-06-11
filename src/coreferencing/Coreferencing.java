//Coreferencing
package coreferencing;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import java.io.File;
import java.util.List;
import java.util.Map;
import edu.stanford.nlp.io.*;
import java.io.*;
import java.util.*;
import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 *
 * @author tejaswineesohoni
 */
public class Coreferencing{
    
    
    public static void main(String[] args){
        //Parse JSON File And Get Text
        JSONParser parser= new JSONParser();
        try {     
                Object obj = parser.parse(new FileReader("input.json"));
                JSONObject jsonObject =  (JSONObject) obj;
                JSONObject response = (JSONObject) jsonObject.get("response");
                String response_text=(String)response.get("plainText");
                System.out.println(response_text);
                PrintWriter out = new PrintWriter("coref_input.txt");
                out.println(response_text);
                out.close();
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }

        try
        {
            ArrayList<String>coreferencedWords=new ArrayList<>();
            ArrayList<Integer>sentenceNumber=new ArrayList<>();
            ArrayList<Integer>chainNumber=new ArrayList<>();
            int corefCounter=0;
            
            //Set Input And Output Files
            FileOutputStream xmlOut = new FileOutputStream(new File("coref_out.xml"));
            String input_filename = "coref_input.txt";
            String file_contents = IOUtils.slurpFileNoExceptions(input_filename);

            //Set Properties
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse, dcoref");
            
            //Annotate And Give Output
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            
            Annotation annotation = new Annotation(file_contents);

            pipeline.annotate(annotation);
            pipeline.xmlPrint(annotation, xmlOut);
            
            
            //For all the sentences in this document, build a CoreMap
            //A CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
            for(CoreMap sentence: sentences) 
            {
              // Traverse the words in the current sentence
              //A CoreLabel is a CoreMap with additional token-specific methods
              for (CoreLabel token: sentence.get(TokensAnnotation.class)) 
              {
                //This is the text of the token
                String word = token.get(TextAnnotation.class);
                //This is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                //This is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);  
              }

              //This is the parse tree of the current sentence
              Tree tree = sentence.get(TreeAnnotation.class);

              // This is the Stanford dependency graph of the current sentence
              SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            }

            // This is the coreference link graph
            // Each chain stores a set of mentions that link to each other
            Map<Integer, CorefChain> graph = annotation.get(CorefChainAnnotation.class);
            //System.out.println(graph);
            
            FileOutputStream Out = new FileOutputStream(new File("coref_output.csv"));
            FileWriter writer = new FileWriter("coref_dataset.csv");
            writer.append("Phrase 1");
	    writer.append(',');
	    writer.append("Phrase 2");
            writer.append(',');
	    writer.append("Sentence 1");
            writer.append(',');
	    writer.append("Sentence 2");
            writer.append(',');
	    writer.append("Chain 1");
            writer.append(',');
	    writer.append("Chain 2");
            writer.append(',');
	    writer.append("Same Sentence");
            writer.append(',');
	    writer.append("Same Chain");
            writer.append(',');
	    writer.append("Size of Chain");
            writer.append(',');
	    writer.append("Result");
	    writer.append('\n');
            
 
	   
            for (Map.Entry<Integer, CorefChain> entry : graph.entrySet())
            {
                int chain=entry.getKey();
                for(Map.Entry<IntPair, Set<CorefMention>> mention : entry.getValue().getMentionMap().entrySet())
                {
                    
                    Set<CorefMention> mentionInstance=mention.getValue();
                    String temp= mentionInstance.toString();
                    String temp1[]=temp.split(" in sentence ");
                    String temp2[]=temp1[0].split("\"");
                    String temp3[]=temp1[1].split("]");
                    int temp4 = Integer.parseInt(temp3[0]);
                    coreferencedWords.add(temp2[1]);
                    sentenceNumber.add(temp4);
                    chainNumber.add(chain);
                    corefCounter++;
                }
                
            }
            
            
            String sameSentence="False";
            String sameChain="False";
            int chainCount=0;
            
            for(int k = 0 ; k < coreferencedWords.size(); k++)
            {
                sameSentence="False";
                sameChain="False";
                for(int j = k+1 ; j < coreferencedWords.size(); j ++)
                {
                    sameSentence="False";
                    sameChain="False";
                    chainCount=0;
                    if(sentenceNumber.get(k).intValue()==sentenceNumber.get(j).intValue())
                    {
                        sameSentence="True";
                    }
                    if(chainNumber.get(k).intValue()==chainNumber.get(j).intValue())
                    {
                        sameChain="True";
                        chainCount=0;
                        int chain=chainNumber.get(j).intValue();
                        System.out.println(chain);
                        for (int l=0;l<chainNumber.size();l++)
                        {
                           if(chainNumber.get(l).intValue()==chain)
                           {
                               chainCount++;
                           }
                        }
                    }
                    System.out.println(coreferencedWords.get(k) + "," + coreferencedWords.get(j)+","+sameSentence+","+sameChain+","+chainCount);
                    writer.append(coreferencedWords.get(k));
                    writer.append(',');
                    writer.append(coreferencedWords.get(j));
                    writer.append(',');
                    writer.append(sentenceNumber.get(k).toString());
                    writer.append(',');
                    writer.append(sentenceNumber.get(j).toString());
                    writer.append(',');
                    writer.append(chainNumber.get(k).toString());
                    writer.append(',');
                    writer.append(chainNumber.get(j).toString());
                    writer.append(',');
                    writer.append(sameSentence);
                    writer.append(',');
                    writer.append(sameChain);
                    writer.append(',');
                    writer.append(String.valueOf(chainCount));
                    writer.append(',');
                    writer.append("");
                    writer.append('\n');
                }    
            }

            
            /*
            ArrayList<List<List<CorefMention>>> results = 
                        new ArrayList<List<List<CorefMention>>>();
                    compute(mentionInstance, new ArrayList<List<CorefMention>>(), results);
                    for (List<List<CorefMention>> result : results)
                    {
                        //System.out.println(result);
                    }
                            
             */
            
            
            System.out.println("Completed");
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }   
}
