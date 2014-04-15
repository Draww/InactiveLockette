package gvlfm78.plugin.InactiveLockette;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class InactiveLocketteUpdateChecker {

    public InactiveLocketteUpdateChecker(InactiveLockette ILUC)
    {
        this.plugin = ILUC;
    }

    private InactiveLockette plugin;
    private URL filesFeed;

    private String version;
    private String link;

    public InactiveLocketteUpdateChecker(InactiveLockette plugin, String url){
        this.plugin = plugin;

        try{
        this.filesFeed = new URL(url);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public boolean updateNeeded(){
        try {
            InputStream input = this.filesFeed.openConnection().getInputStream();
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

            Node latestFile = document.getElementsByTagName("item").item(0);
            NodeList children = latestFile.getChildNodes();

            this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
            this.link = children.item(3).getTextContent();

            if(!plugin.getDescription().getVersion().equals(this.version)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    public String getVersion(){
        return this.version;
    }

    public String getLink(){
        return this.link;
    }

}
