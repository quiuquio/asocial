package org.me.asocial.server;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.catalina.util.Base64;
//trovi tutto qui -> http://stackoverflow.com/a/9038038/814180
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// **********************************************************
// ************************ CLASSES *************************
// **********************************************************

class Database 
{
	Connection con;
	PreparedStatement pst;
	ResultSet rs;
	Database()
	{
		try{
			Class.forName("com.mysql.jdbc.Driver");
			con=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/asocial_db","root","");
                        //con=DriverManager.getConnection("jdbc:mysql://localhost:3306/asocial_db","asocial_god","asocial");
		   }
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}

	protected int checkLogin(String usr,String pwd)
	{
		try {
                    String query="SELECT * FROM user_authentication WHERE username=? AND password=?";
                    pst=con.prepareStatement(query);
                    pst.setString(1, usr);
                    pst.setString(2, pwd);
                    rs=pst.executeQuery();
                    if(rs.next())
                    {
			return Integer.parseInt(rs.getString("user_id"));                      
                    } else {
			return -1;
                    }
		} catch (Exception e) {
                    return -1;
		}
        }
        
        protected String getUserName(int userID)
	{
		try {
                    String query="SELECT * FROM user_authentication WHERE user_id=?";
                    pst=con.prepareStatement(query);
                    pst.setString(1, String.valueOf(userID));
                    rs=pst.executeQuery();
                    if(rs.next())
                    {
			return rs.getString("username");                      
                    } 
                    else{
                        return "error";
                    }
		} catch (Exception e) {
                    return "error";
		}
        }
        
        protected String setPost(int userID, String postTitle, String postBody)
        {
                try {
                    String query="SELECT `user_authentication`.`username` FROM `user_authentication` WHERE `user_authentication`.`user_id`=?";
                    pst=con.prepareStatement(query);
                    pst.setInt(1, userID);
                    rs=pst.executeQuery();
                    String userName="";
                    if(rs.next()){
                        userName=rs.getString("username");
                    }
                    pst.clearParameters();
                    query="INSERT INTO `asocial_db`.`posts` (`post_id`, `post_date`, `post_title`, `post_body`, `user_id`, `username`)"
                                + "VALUES (NULL, CURRENT_TIMESTAMP,?,?,?,?)";
                    pst=con.prepareStatement(query);
                    pst.setString(1, postTitle);
                    pst.setString(2, postBody);
                    pst.setInt(3, userID);
                    pst.setString(4, userName);
                    pst.executeUpdate();
                } catch (Exception e){
                    return "Errore! " + e;
                }
          return "Post inserito!";  
        }
        
        protected String registrationRequest(String username, String password)
        {
                try {
                    String query="SELECT `user_authentication`.`username` FROM `asocial_db`.`user_authentication` WHERE `username`=?";
                    pst=con.prepareStatement(query);
                    pst.setString(1, username);
                    rs=pst.executeQuery();
                    if(rs.next()) {
                        return "Nome utente già in uso!";
                    }
                    pst.close();
                    query="INSERT INTO `asocial_db`.`user_authentication` (`username`, `password`, `user_id`)" 
                            + "VALUES (?, ?, NULL)";
                    pst=con.prepareStatement(query);
                    pst.setString(1, username);
                    pst.setString(2, password);
                    pst.executeUpdate();
                } catch (Exception e) {
                    return "Errore! " + e;
                }
                    return "Registrazione effettuata!";
        }
        
        protected ResultSet getPost()
        {
                try {
                    String query="SELECT * FROM posts ORDER BY `post_date` DESC";
                    pst=con.prepareStatement(query);
                    rs=pst.executeQuery();
                } catch (Exception e) {
                    System.out.println("Errore: " + e);
                    return null;
                }
            return rs;
        }
        
        protected String setComment(int userID, int postID, String commentBody)
        {
                try {
                    String query="SELECT `user_authentication`.`username` FROM `user_authentication` WHERE `user_authentication`.`user_id`=?";
                    pst=con.prepareStatement(query);
                    pst.setInt(1, userID);
                    rs=pst.executeQuery();
                    String userName="";
                    if(rs.next()){
                        userName=rs.getString("username");
                    }
                    query="INSERT INTO `asocial_db`.`post_comments` (`user_id`, `post_id`, `comment_id`, `comment_body`, `comment_date`, `username`)"
                                + "VALUES (?, ?, NULL, ?, CURRENT_TIMESTAMP, ?)";
                    pst=con.prepareStatement(query);
                    pst.setInt(1,userID);
                    pst.setInt(2, postID);
                    pst.setString(3, commentBody);  
                    pst.setString(4, userName);
                    pst.executeUpdate();
                } catch (Exception e){
                    return "Errore! " + e;
                }
          return "Commento inserito!";  
        }
        
        protected String remvComment(int commentID)
        {
            try{
                String query = "DELETE FROM `post_comments` WHERE `comment_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, commentID);
                pst.executeUpdate();
            } catch (Exception e){
                return "Errore! " + e;
             }
            return "Commento rimosso con successo!";
        }
        
        protected Boolean remvPost(int postID)
        {
            try{
                String query = "DELETE FROM `posts` WHERE `post_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, postID);
                pst.executeUpdate();
                
                query = "DELETE FROM `post_comments` WHERE `post_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, postID);
                pst.executeUpdate();
                
                return true;                
            } catch (Exception e){
                return false;
             }
        }
        
        protected int remvUser(int userID)
        {
            try{
                String query = "DELETE FROM `user_authentication` WHERE `username` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                return pst.executeUpdate();
            } catch (Exception e){
                return -1;
             }
        }
        
        protected void hatePost(int postID)
        {
            try{
                String query = "UPDATE `posts` SET `hate` = `hate`+1 WHERE `post_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, postID);
                pst.executeUpdate();
            } catch (Exception e){
             }
        }
        
        protected void hateComment(int commentID)
        {
            try{
                String query = "UPDATE `post_comments` SET `hate` = `hate`+1 WHERE `comment_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, commentID);
                pst.executeUpdate();
            } catch (Exception e){
             }
        }
        
        protected ResultSet getComment()
        {
                try {
                    String query="SELECT * FROM post_comments ORDER BY `comment_date` ASC";
                    pst=con.prepareStatement(query);
                    rs=pst.executeQuery();
                } catch (Exception e) {
                    System.out.println("Errore: " + e);
                    return null;
                }
            return rs;
        }
        
        protected int getAvatar(int userID){
            try{
                String query="SELECT `avatar` FROM `user_authentication` WHERE `user_id`=?";

                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                rs=pst.executeQuery();
                while(rs.next())
                {
                    return rs.getInt(1);
                } return 0;
            } catch (Exception e) {
                return 0;
            }
        }
        
        protected Boolean setAvatar(int userID){
            try{                                        
                String query="UPDATE `user_authentication` SET `avatar`=1 WHERE `user_id`=?";
                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                pst.executeUpdate();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
         
        protected void setAdmin(int userID){
            try{
                String query="INSERT INTO `admin`(`user_id`, `admin_level`) VALUES (?, ?)";
                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                pst.setInt(2, 2);
                pst.executeUpdate();
            } catch (Exception e) {
            }
        }
        
        protected void remvAdmin(int userID){
            try{
                String query="DELETE FROM `admin` WHERE `user_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                pst.executeUpdate();
            } catch (Exception e) {
}
        }

        protected int isAdmin(int userID)
	{
		try {
                    String query="SELECT `admin_level` FROM `admin` WHERE `user_id` = ?";
                    pst=con.prepareStatement(query);
                    pst.setInt(1, userID);
                    rs=pst.executeQuery();
                    if(rs.next())
                    {
			return rs.getInt("admin_level");                 
                    } else {
			return -1;
                    }
		} catch (Exception e) {
                    return -1;
		}
        }
         
        protected String getUserBio(int userID)
        {
            try {
                String query="SELECT `bio` FROM `user_authentication` WHERE `user_id` = ?";
                pst=con.prepareStatement(query);
                pst.setInt(1, userID);
                rs=pst.executeQuery();
                rs.next();
                return rs.getString("bio");
            } catch (Exception e){
                return "";
            }
        }
        
        protected Boolean setUserBio(int userID, String bio)
        {
            try {
                String query="UPDATE `user_authentication` SET `bio` = ? WHERE `user_id` = ?";
                pst=con.prepareStatement(query);
                pst.setString(1, bio);
                pst.setInt(2, userID);
                pst.executeUpdate();
                return true;
            } catch (Exception e){
                return false;
}
        }

}

class XMLPostFile 
{
        //String post_repo_xml="C:\\xampp\\htdocs\\ASocialClient\\file.xml";
        String post_repo_xml="C:\\Program Files (x86)\\EasyPHP-12.1\\www\\ASocialSite\\file.xml";
 
	protected boolean getXML() {
                Database db = new Database();
                ResultSet xrs=db.getPost();
                
            try {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                  
                Document doc = docBuilder.newDocument();
                Element post_root = doc.createElement("post_root");
                doc.appendChild(post_root);
                
                while (xrs.next()) {
                    String ptitle = xrs.getString("post_title");
                    String pbody = xrs.getString("post_body");
                    String uname = xrs.getString("username");
                    Timestamp pdate = xrs.getTimestamp("post_date");
                    int pid = xrs.getInt("post_id");
                    int uid = xrs.getInt("user_id");
                    int hate = xrs.getInt("hate");

 
                    Element post = doc.createElement("post");
                    post_root.appendChild(post);
                    
                    Element post_id = doc.createElement("post_id");
                    post_id.appendChild(doc.createTextNode(Integer.toString(pid)));
                    post.appendChild(post_id);
                    
                    Element user_id = doc.createElement("user_id");
                    user_id.appendChild(doc.createTextNode(Integer.toString(uid))); 
                    post.appendChild(user_id);

                    Element post_date = doc.createElement("post_date");
                    post_date.appendChild(doc.createTextNode(String.format("%1$TD %1$TT", pdate)));
                    post.appendChild(post_date);

                    Element post_title = doc.createElement("post_title");
                    post_title.appendChild(doc.createTextNode(ptitle));
                    post.appendChild(post_title);

                    Element post_body = doc.createElement("post_body");
                    post_body.appendChild(doc.createTextNode(pbody));
                    post.appendChild(post_body);
                    
                    Element username = doc.createElement("username");
                    username.appendChild(doc.createTextNode(uname));
                    post.appendChild(username);
                    
                    Element hate_counter = doc.createElement("hate");
                    hate_counter.appendChild(doc.createTextNode (Integer.toString(hate)));
                    post.appendChild(hate_counter);   
                }
 
		// Scrive nel file XML
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(post_repo_xml));
 		
 		transformer.transform(source, result);
 
		return true;
                
 	  } catch (SQLException ex) {
                System.out.println(ex);
                return false;
        } catch (ParserConfigurationException pce) {
              System.out.println(pce);
              return false;
	  } catch (TransformerException tfe) {
              System.out.println(tfe);
              return false;
	  }
	}
}

// sostituisce un URL completo di protocollo con <a href= url> WRL </a>...
class URLInString 
{
    protected static String findURL(String s) {
        // separa l'input per spazi ( un URL non ha spazi )
        String [] parts = s.split("\\s");
        String pattern = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";
        String out = new String();
        Pattern compiledPattern;
        Matcher matcher;

        // Cerca di convertire ogni parte in un URL  
        for( String item : parts ){ try {
            URL url = new URL(item);
            // Se possibile aggiunge le ancore
            out = out +"<a href=\"" + url + "\">"+ url + "</a> ";

            compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            matcher = compiledPattern.matcher(item);
            while(matcher.find()) {
                //System.out.println(matcher.group());
                out = out
               +"<iframe class='youtube-player' type='text/html' src='http://www.youtube.com/embed/"
               + matcher.group(1)
               +"' frameborder='0'></iframe>";
                //width='640' height='385'
                
                
            }
        } catch (MalformedURLException e) {
            // se c'era un URL non era questo...
            //System.out.print( item + " " );
            out = out + item + " ";
        }
        }
        return out;
    }
}

class XMLCommentsFile
{
        //String post_repo_xml="C:\\xampp\\htdocs\\ASocialClient\\commentsfile.xml";
        String post_repo_xml="C:\\Program Files (x86)\\EasyPHP-12.1\\www\\ASocialSite\\commentsfile.xml";
 
	protected boolean getXML() {
                Database db = new Database();
                ResultSet xrs=db.getComment();
                
            try {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                  
                Document doc = docBuilder.newDocument();
                Element comment_root = doc.createElement("comment_root");
                doc.appendChild(comment_root);
                
                while (xrs.next()) {
                    String cbody = xrs.getString("comment_body");
                    String uname = xrs.getString("username");
                    int cid = xrs.getInt("comment_id");
                    int uid = xrs.getInt("user_id");
                    int pid = xrs.getInt("post_id");
                    int hate = xrs.getInt("hate");
                    Timestamp cdate = xrs.getTimestamp("comment_date");
 
                    Element comment = doc.createElement("comment");
                    comment_root.appendChild(comment);
                    
                    Element post_id = doc.createElement("post_id");
                    post_id.appendChild(doc.createTextNode(Integer.toString(pid)));
                    comment.appendChild(post_id);
                    
                    Element user_id = doc.createElement("user_id");
                    user_id.appendChild(doc.createTextNode(Integer.toString(uid)));
                    comment.appendChild(user_id);
                    
                    Element comment_id = doc.createElement("comment_id");
                    comment_id.appendChild(doc.createTextNode(Integer.toString(cid)));
                    comment.appendChild(comment_id);
                  
                    Element comment_date = doc.createElement("comment_date");
                    comment_date.appendChild(doc.createTextNode(String.format("%1$TD %1$TT", cdate)));
                    comment.appendChild(comment_date);

                    Element comment_body = doc.createElement("comment_body");
                    comment_body.appendChild(doc.createTextNode(cbody));
                    comment.appendChild(comment_body);
                    
                    Element username = doc.createElement("username");
                    username.appendChild(doc.createTextNode(uname));
                    comment.appendChild(username);
                    
                    Element hate_counter = doc.createElement("hate");
                    hate_counter.appendChild(doc.createTextNode(Integer.toString(hate)));
                    comment.appendChild(hate_counter);
                }
 
		// Scrive nel file XML
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(post_repo_xml));
 		
 		transformer.transform(source, result);
 
		return true;
                
 	  } catch (SQLException ex) {
                System.out.println(ex);
                return false;
        } catch (ParserConfigurationException pce) {
              System.out.println(pce);
              return false;
	  } catch (TransformerException tfe) {
              System.out.println(tfe);
              return false;
	  }
	}
}

class ImageResize
{

    public static String getScaledInstance(    String addres,
                                               //String resizedAddres,
                                               int targetWidth,
                                               int targetHeight,
                                               boolean higherQuality)
    {
        BufferedImage img;
        File target;
        //target = new File(resizedAddres);
        try{
            //target = new File(resizedAddres);
            target = File.createTempFile("tmp-img", ".jpeg");
    	}catch(IOException e){
            System.out.println(e.getMessage());
            return "ERRORE";
    	}
        try {
            img = ImageIO.read(new File(addres));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "Errore";
        }
        
        //Codice per fare riduzioni percentuali invece che deterministiche
        //Potrebbe servire...
        /*
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        int wReduction = (int)((imgW * targetWidth)/100);
        int hReduction = (int)((imgH * targetHeight)/100);
        targetWidth = imgW - wReduction;
        targetHeight = imgH - hReduction;
        * 
        */
        
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            /*  Utilizza una tecnica multi-step: Si parte dalle dimmensioni 
             *  originali. Con O(log n) passaggi, dove n è la dim dell'immagine,
             *  si raggiungono le dimmensioni desiderate. 
             *  Si utilizza inoltre drawImage() per ridurre l'overhead.
             */
            w = img.getWidth();
            h = img.getHeight();
        } else {
            /*
             * Utilizza una tecnica single-step: Dalle dimensioni originali si
             * passa direttamente a quella desiderata.
             * Più veloce ma di peggiore qualità.
             */
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
            
            try{
                ImageIO.write(ret, "jpg", target);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return "ERRORE";
            }
        } while (w != targetWidth || h != targetHeight);  
        
        try {
            String ris;
            ris = Base64.encode(FileUtils.readFileToByteArray(target));
            target.delete();
            return ris;
        } catch (IOException ex) {
            Logger.getLogger(ImageResize.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERRORE";
    }

}

// **********************************************************
// *************** WEB SERVICE ASocialService ***************
// **********************************************************

@WebService(serviceName = "ASocialService")
public class ASocialService {
    
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    @WebMethod(operationName = "loginRequest")
    public int loginRequest(    @WebParam(name = "username") String username,
                                @WebParam(name = "password") String password) {
        Database db = new Database();
        return db.checkLogin(username,password);
    }
    
    @WebMethod(operationName = "getUserName")
    public String getUserName(@WebParam(name = "userID") int userID) {
        Database db = new Database();
        return db.getUserName(userID);
    }
    
    @WebMethod(operationName = "registrationRequest")
    public String registrationRequest(  @WebParam(name = "username") String username,
                                        @WebParam(name = "password") String password,
                                        @WebParam(name = "pwconfirm") String pwconfirm) {
        Database db = new Database();
        if(password.equals(pwconfirm)) {
            return db.registrationRequest(username, password);
        }
        return "le password non corrispondono!";
    }

    @WebMethod(operationName = "setPost")
    public String setPost(  @WebParam(name = "userID") int userID,
                            @WebParam(name = "postTitle") String postTitle, 
                            @WebParam(name = "postBody") String postBody) 
    {
        Database db = new Database();
        String res = db.setPost(userID, postTitle, postBody);
        return res;
    }

    @WebMethod(operationName = "updatePostXML")
    public boolean updatePostXML() {
        XMLCommentsFile xmlcom = new XMLCommentsFile();
        xmlcom.getXML();
        XMLPostFile xml = new XMLPostFile();
        boolean res = xml.getXML();
        return res;
    }
    
    @WebMethod(operationName = "URLify")
    public String URLify( @WebParam(name = "str") String str){
        String res = URLInString.findURL(str);
        return res;
    }

    @WebMethod(operationName = "setComment")
    public String setComment(   @WebParam(name = "userID") int userID,
                                @WebParam(name = "postID") int postID,  
                                @WebParam(name = "commentBody") String commentBody)
    {
        Database db = new Database();
        String res = db.setComment(userID, postID, commentBody);
        return res;
    }
    
    @WebMethod(operationName = "remvComment")
    public void remvComment(  @WebParam(name = "commentID") int commentID)
    {
        Database db = new Database();
        db.remvComment(commentID);
    }
    
    @WebMethod(operationName = "remvPost")
    public Boolean remvPost(  @WebParam(name = "postID") int postID)
    {
        Database db = new Database();
        return db.remvPost(postID);
    }
    
    @WebMethod(operationName = "remvUser")
    public int remvUser(  @WebParam(name = "userID") int userID)
    {
        Database db = new Database();
        return db.remvUser(userID);
    }
    
    @WebMethod(operationName = "hatePost")
    public void hatePost(  @WebParam(name = "postID") int postID)
    {
        Database db = new Database();
        db.hatePost(postID);
    }
    
    @WebMethod(operationName = "hateComment")
    public void hateComment(  @WebParam(name = "commentID") int commentID)
    {
        Database db = new Database();
        db.hateComment(commentID);
    }
    
    @WebMethod(operationName = "checkAvatar")
    public int checkAvatar(@WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        int res = db.getAvatar(userID);
        return res;
    }
    
    @WebMethod(operationName = "setAvatar")
    public Boolean setAvatar(   @WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        Boolean res = db.setAvatar(userID);
        return res;
    }
    
    @WebMethod(operationName = "setAdmin")
    public void setAdmin(@WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        db.setAdmin(userID);
    }
    
    @WebMethod(operationName = "remvAdmin")
    public void remvAdmin(@WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        db.remvAdmin(userID);
    }
    
    @WebMethod(operationName = "isAdmin")
    public int isAdmin(@WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        return db.isAdmin(userID);
    }
    
    /*@WebMethod(operationName = "resizeImmage")
    public Boolean resizeImmage (   @WebParam(name = "image") String image,
                                    @WebParam(name ="resizedAddres") String resizedAddres,
                                    @WebParam(name = "HD") Boolean higherQuality
                                        )
    {     
        //WebParam(name = "targetW") int targetW,
        //WebParam(name = "targetH") int targetH,
        //passo h e w di default= 50
        Boolean res = ImageResize.getScaledInstance(image, resizedAddres, 50, 50, higherQuality);
        return  res;
    }*/
    
    
    @WebMethod(operationName = "resizeImmage")
    public String resizeImmage (   @WebParam(name = "image") String image,
                                    //@WebParam(name ="resizedAddres") String resizedAddres,
                                    @WebParam(name ="width") int width,
                                    @WebParam(name ="height") int height,                            
                                    @WebParam(name = "HD") Boolean higherQuality
                                        )
    {     
        //WebParam(name = "targetW") int targetW,
        //WebParam(name = "targetH") int targetH,
        String res = ImageResize.getScaledInstance(image, width, height, higherQuality);
        return  res;
    }
    
    @WebMethod(operationName = "getUserBio")
    public String getUserBio(   @WebParam(name ="userID") int userID)
    {
        Database db = new Database();
        String res = db.getUserBio(userID);
        if(res == null){
            res = "";
}
        return res;
    }
    
    @WebMethod(operationName = "setUserBio")
    public Boolean setUserBio(   @WebParam(name ="userID") int userID,
                                @WebParam(name ="bio") String bio)
    {
        Database db = new Database();
        return db.setUserBio(userID, bio);
    }  
}
