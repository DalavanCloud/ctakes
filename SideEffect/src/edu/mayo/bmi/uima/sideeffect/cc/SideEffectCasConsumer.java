package edu.mayo.bmi.uima.sideeffect.cc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.sideeffect.type.SideEffectAnnotation;
import edu.mayo.bmi.uima.sideeffect.util.SEUtil;

public class SideEffectCasConsumer extends CasConsumer_ImplBase {
	public static final String PARAM_OUTPUT_FILE = "OutputFile";
	public static final String PARAM_DELIMITER = "Delimiter";  
	private BufferedWriter iv_bw = null;
	private String iv_delimiter;

	public void initialize() throws ResourceInitializationException {
		File outFile;

		try
		{
			String filename = (String) getConfigParameterValue(PARAM_OUTPUT_FILE);
			outFile = new File(filename);
			if (!outFile.exists())
				outFile.createNewFile();
			iv_bw = new BufferedWriter(new FileWriter(outFile));
			//iv_bw = new BufferedWriter(new FileWriter(filename, true));

			iv_delimiter = (String) getConfigParameterValue(PARAM_DELIMITER);

		} catch (Exception ioe)
		{
			throw new ResourceInitializationException(ioe);
		}
	}


	
	public void processCas(CAS cas) throws ResourceProcessException {
		try {
			JCas jcas;
			
			jcas = SEUtil.getJCasViewWithDefault(cas, "plaintext");
			
			if(jcas == null){
				jcas = cas.getJCas(); 
			}
			
			JFSIndexRepository indexes = jcas.getJFSIndexRepository();			
			
	        String docName = DocumentIDAnnotationUtil.getDocumentID(jcas);        

	        Iterator seIter = indexes.getAnnotationIndex(SideEffectAnnotation.type).iterator();
	        while(seIter.hasNext()) {
	        	SideEffectAnnotation sea = (SideEffectAnnotation) seIter.next();
	        	String se = "";
	        	String seSpan = "";
	        	String drug = "";
	        	String drugSpan = "";
	        	String sentence = sea.getSentence().getCoveredText().trim();
	        	
        		se = sea.getCoveredText().trim();
        		seSpan = Integer.toString(sea.getBegin()) + iv_delimiter 
        					+ Integer.toString(sea.getEnd());
        		if(sea.getDrug()!=null) {
        			drug = sea.getDrug().getCoveredText().trim();
        			drugSpan = Integer.toString(sea.getDrug().getBegin()) + iv_delimiter 
        						+ Integer.toString(sea.getDrug().getEnd());
        		}
        		else {
        			drug = se;
        			drugSpan = seSpan;
        		}
        		
	        	String output = docName + iv_delimiter + se + iv_delimiter + seSpan +
	        			iv_delimiter + drug + iv_delimiter + drugSpan + iv_delimiter + 
	        			sentence;
	        	
	        	iv_bw.write(output+"\n");
	        }

		} catch (Exception e) {
			throw new ResourceProcessException(e);
		}
	}
	
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException, IOException
	{
		super.collectionProcessComplete(arg0);

		try
		{
			iv_bw.flush();
			iv_bw.close();
		}
		catch(Exception e)
		{ throw new ResourceProcessException(e); }
	}
}