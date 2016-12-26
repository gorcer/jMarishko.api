package com.gorcer.jMarishko.api;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class Word2VecMgr {
	
	
	private static Logger log = LoggerFactory.getLogger(Word2VecMgr.class);
	private static String pathToVectors="/data/vectors.dat";

	
	private static void prepareDictionary(String sourceFile) throws Exception {
		// Gets Path to Text file
        //String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();
    	String appPath = new File(".").getCanonicalPath();
    	
        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(appPath + sourceFile);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Write word vectors to file
        //WordVectorSerializer.writeWordVectors(vec, appPath + pathToVectors);
        WordVectorSerializer.writeWord2VecModel(vec, appPath + pathToVectors);

	}
	
    public static void main(String[] args) throws Exception {
    	
    	String appPath = new File(".").getCanonicalPath();
    	/*
    	String sourceFile = "/tmp/source.txt";
    	
    	DBManager.Connect();
    	DBManager.saveTalkToFile(sourceFile);
    	prepareDictionary(sourceFile);
*/
    	Word2Vec vec = WordVectorSerializer.readWord2Vec(new File (appPath + pathToVectors));
    	
    	
        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");
        Collection<String> lst = vec.wordsNearest("пить", 10);
        System.out.println("10 Words closest to 'привет': " + lst);
        
        
        
        System.out.println("Здарова: " + vec.getWordVector("здарова").toString());
        System.out.println("Здравствуйте: " + vec.similarity("привет", "здравствуйте"));
        System.out.println("hi: " + vec.similarity("привет", "hi"));        
        System.out.println("привет: " + vec.similarity("привет", "привет"));
        

        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());
    }
}
