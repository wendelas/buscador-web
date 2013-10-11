package test.busca;

import java.io.IOException;

import net.visualizacao.util.UtilBusca;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;


public class Busca {

	@Test
	   public void buscarNome()  {

		try {
			UtilBusca util = new UtilBusca(System.getProperty("user.home")
					+ "/dados/indices/Nomes");
			TopDocs docs = util.buscar("raphael");
			ScoreDoc[] scoreDocs = docs.scoreDocs;
			System.out.println("Foram encontrados " + docs.totalHits + " ocorrências");
	         for (int i = 0; i < scoreDocs.length; i++)
	         {
	            Document hitDoc = util.getBuscador().doc(scoreDocs[i].doc);
	            System.out.println("Conteúdo: " + hitDoc.get("TextoCompleto"));	            
	         }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
