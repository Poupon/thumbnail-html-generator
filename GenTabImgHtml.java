import java.io.*;
import java.util.*;
import java.lang.*;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.*;
//import java.awt.Color;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;

//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;



/***********************T
Principes:

On creer un fichier thg_*.html avec des commentaires speciaux 
<#CmdTabThumb SRC=img2 THUMB_SIZE=64  COLUMNS=4 #>
<#CmdInclude nomfichier #>

des sous repertoires avec des images

et le prog creer les fichier *.html avec des miniatures (dans des sous repertoires)
(************************/



//*************************************************

class Parameter{
		
    public static String sDefaultFile="thg.config";
    public static TreeSet<String> sSetExtension  = new TreeSet<String>();
    


    public String cSrcDir     = "thg_img";
    public String cDestExt    = "thumb_"; //OK
    public int    cThumbSize  = 128;
    public int    cIconHeight = 128;
    public int    cColumns    = 5;
    public boolean cFlagSize   = false;
    public boolean cFlagName   = false;
    public boolean cFlagWH     = false;
    public String cTabBorder   = "2";
   public  String cFormat  = "jpg";

    public Parameter( ){
	sSetExtension.add( "png" );
	sSetExtension.add( "jpg" );
	sSetExtension.add( "jpeg" );
	sSetExtension.add( "gif" );
    }
    
    public Parameter( Parameter pParam ){
	
	cSrcDir     = new String( pParam.cSrcDir );
	cDestExt    = new String( pParam.cDestExt );
	cThumbSize  = pParam.cThumbSize;
	cIconHeight = pParam.cIconHeight;
	cColumns    = pParam.cColumns;
	cFlagSize   = pParam.cFlagSize;
	cFlagName   = pParam.cFlagName;
	cFlagWH     = pParam.cFlagWH;
	cTabBorder   = pParam.cTabBorder;
	cFormat  = pParam.cFormat;

	//				cSetExtension = new TreeSet<String>( pParam.cSetExtension );	 
    }

    void debug(){
	System.out.println( "== Parameter cSrcDir:" + cSrcDir +
			    " cDestExt:" + cDestExt+
			    " cThumbSize" +  cThumbSize+
			    " cIconHeight" + cIconHeight+
			    " cColumns" + cColumns
			    );
    }
    public void readFromString( String pStr ){
	
	StringTokenizer lTok = new StringTokenizer(pStr.substring(0));
	while( lTok.hasMoreTokens() ){
	    
	    String lTmp  = lTok.nextToken(" \t").trim();
	    
						//						System.out.println( "Tmp:" + lTmp );
	    
	    if( lTmp.startsWith( "SRC=" ) ){
		cSrcDir = lTmp.substring( 4 ).trim();
	    }
	    else	if( lTmp.startsWith( "THUMB_SIZE=" )  ){
		cThumbSize = Integer.valueOf( lTmp.substring( 11 ).trim() );
	    }
	    else	if( lTmp.startsWith( "COLUMNS=" )){
		cColumns = Integer.valueOf( lTmp.substring( 8 ).trim());
	    }
	    else	if( lTmp.startsWith( "SIZE=" )){
		cFlagSize = lTmp.substring( 5 ).trim().startsWith("1");
	    }
	    else	if( lTmp.startsWith( "NAME=" )){
		cFlagName = lTmp.substring( 5 ).trim().startsWith("1");
	    }
	    else	if( lTmp.startsWith( "WH=" )){
		cFlagWH = lTmp.substring( 3 ).trim().startsWith("1");
	    }
	    else	if( lTmp.startsWith( "BORDER=" )){
		cTabBorder = lTmp.substring( 7 ).trim();
	    }
	    else	if( lTmp.startsWith( "FORMAT=" )){
		cFormat = lTmp.substring( 7 ).trim();
		System.out.println( "=================== FORMAT=" + cFormat );
	    }
	}
    }		
}

//*************************************************

class GenTabImgHtml  {
    
    
    public static String sTypeFilePrefix="thg_"; // OK

    public static boolean sEco = false;

    public static String sOutputPath  = null;
   public static String sFilterExtension = "html";

    static String sSign="<!--#";
    //=======
    public enum Command{
	CMD_TAB_THUMB( "CmdTabThumb" ),
	CMD_INCLUDE(   "CmdInclude" ),
	CMD_END(       "#-->" ),
	CMD_TAB_BEGIN( "CmdTabBegin" ),
	CMD_TAB_END( "CmdTabEnd" ),
	;
	
	final String cStrCmd;

	Command( String pStr ){
	    cStrCmd = pStr;
	}
	
	public static final HashMap<String,Command> sHashCommande = new HashMap<String,Command>();
	
	
	static {
	    for( Command f: Command.values() ){
		sHashCommande.put( f.cStrCmd, f );
	    }
	}
    }
    //=======
    
    
    
    //------------------------------------------------
		
    GenTabImgHtml(){
    }
    
    //------------------------------------------------
    
    void writeln( BufferedWriter pBufWrite, String lStr ) throws IOException{
	
	pBufWrite.write( lStr, 0, lStr.length() );		
	pBufWrite.newLine();									
    }
    void write( BufferedWriter pBufWrite, String lStr ) throws IOException{
	
	pBufWrite.write( lStr, 0, lStr.length() );		
	pBufWrite.newLine();									
    }
    //------------------------------------------------
    //------------------------------------------------
    // Creation d'une image miniature

    boolean createMiniature(  String pStrImg, String pDestImg, Parameter pParam, Point lSizeThumb, Point lSizeImg ){
	
	ImageIcon 	lIcon  = new ImageIcon( pStrImg ); 
	
	lSizeImg.setLocation( lIcon.getIconWidth(), lIcon.getIconHeight() );

	if( GenTabImgHtml.sEco ) // NEW NEW NEW 
	    return true;

	 //lancer en multithread !!! 
	// Demander un thread d'un pool et si dispos lancer sinon attendre !	    
	//int getCorePoolSize()
	//int getMaximumPoolSize()

	float lDiv=1;
	if( lIcon.getIconWidth()  > lIcon.getIconHeight() ){
	    lDiv = lIcon.getIconWidth() / (float)pParam.cThumbSize;
	}
	else
	    lDiv = lIcon.getIconHeight() / (float)pParam.cThumbSize;
	
	lSizeThumb.setLocation( lIcon.getIconWidth()/lDiv, lIcon.getIconHeight()/lDiv );
	
	
	
	
	// celon le mode de calcul del taille doit  varier A FAIRE
	Image lImageScale = lIcon.getImage().getScaledInstance( (int)lSizeThumb.getX(), (int)lSizeThumb.getY(), Image.SCALE_SMOOTH );
	lIcon  = new ImageIcon(lImageScale  ); 
	
	// AJOUTER CONTROLE ERREUR !!
	
	BufferedImage lBuffIn = new BufferedImage( (int)lSizeThumb.getX(), (int)lSizeThumb.getY(), BufferedImage.TYPE_INT_RGB );
	Graphics2D lGrafIn = (Graphics2D)lBuffIn.getGraphics();
	lGrafIn.drawImage( lIcon.getImage(), 0, 0, lIcon.getImageObserver() );
	
	Iterator lIterWriters = ImageIO.getImageWritersByFormatName(pParam.cFormat); // ou png
	ImageWriter lWriter = (ImageWriter)lIterWriters.next();
	
	
	try {
	    File lIConFile = new File( pDestImg );
	    
	    ImageOutputStream llIOs = ImageIO.createImageOutputStream(lIConFile);
	    lWriter.setOutput(llIOs);
						
	    
	    //    Finally, the image may be written to the output stream:
	    
	    lWriter.write(lBuffIn);
	}
	catch(Exception e){
	    System.err.println( e );
	}	
	return true;
    }
    
    //---------------------------------
    //---------------------------------
    // --- PROCESS
    //---------------------------------
    //---------------------------------
    // creation du code html pour les tab et generation des miniatures

    public boolean makeTab( String pPath, String pSbuf, BufferedWriter pBufWrite, Parameter pParam) throws IOException {
			
	
	System.out.println( "makeTab Path:" + pPath + " Buf:"+ pSbuf );
	
	Parameter lLocalParam = new Parameter( pParam );
	
	// Mise a jour des parametres 
	lLocalParam.readFromString( pSbuf );
	
	String lLocalPath = pPath +"/"+lLocalParam.cSrcDir;

	File lFile = new File( lLocalPath );
	
	if( lFile.isDirectory() == false ){
	    System.err.println( "Error <" + lLocalPath + "> is not a directory");
	    return false;
	}
	
	
	
	String lEntry[] = lFile.list( );

	writeln( pBufWrite, "<!-- generation des thumbails par GenTabImgHtml (Philippe Poupon)-->");	

	
	// Create table declaration
	
	writeln( pBufWrite, "<br>" );
	writeln( pBufWrite, "<center>" );
	writeln( pBufWrite, "<table border=\""+lLocalParam.cTabBorder+"\">");
	writeln( pBufWrite, "    <tbody>");
	
	int lColumns = 0;
	for( int i=0; i< lEntry.length; i++) {
	    String lFileName = lEntry[i];

	    System.out.println( "\t>" + lFileName);
	    
	    int lIndexExtension = lFileName.lastIndexOf(".");
	    if( lIndexExtension == -1 ){
		
		System.err.println( "\t\no extension" );
		continue ;
	    }
	    
	    String lExtension = lFileName.substring( lIndexExtension+1 );
	    
	    if( Parameter.sSetExtension.contains( lExtension.toLowerCase() ) == false){
		// extension non gere par le programme
		System.err.println( "\t\tbad extension:" + lExtension );
		continue;
	    }
	    
	    String lDestFileNameHead = lFileName.substring( 0, lIndexExtension );

	    // C'est bien un fichier image !
	    
	    String lSrcFilePath = pPath + "/" +lLocalParam.cSrcDir  + "/" +lEntry[i];
	    
	    String lDestDirName = lLocalParam.cDestExt  + lLocalParam.cSrcDir ;

	    String lDestDirPath = pPath  + "/" + lDestDirName;


	    if( sOutputPath!= null ) // NEW NEW NEW 
		lDestDirPath = sOutputPath + lLocalParam.cSrcDir;;

	    
	    
	    File lFileSrc = new File( lSrcFilePath );
	    if( lFileSrc.canRead() == false ){
		System.err.println( "Error can read file:" + lSrcFilePath );
		continue;
	    }

	    

	    System.out.println( "OPath=" + sOutputPath +"->" + lDestDirPath );


	    File lDestDirFile  = new File( lDestDirPath );

	    lDestDirFile.mkdirs();
	    
	    //	    String lDestFileName =  LocalParam.cDestExt + lEntry[i];
	    String lDestFileName =  lLocalParam.cDestExt  + lDestFileNameHead + '.' + lLocalParam.cFormat;
	    String lDestFilePath =  lDestDirPath + "/" + lDestFileName;

	    
	    Point lSizeThumb = new Point(0,0);
	    Point lSizeImg   = new Point(0,0);

	    /*	    if(  GenTabImgHtml.sEco == false
		 || lLocalParam.cFlagSize == true
		 || lLocalParam.cFlagWH == true ){		
		}*/
	    createMiniature( lSrcFilePath, lDestFilePath, lLocalParam, lSizeThumb, lSizeImg);

	    // Create Line for tab img
	    //						process(  lPath, lLocalParam, lDecal );
	    if( lColumns==0)
		writeln( pBufWrite, "    <tr>");
	    
	    writeln( pBufWrite, "<td align=\"center\" valign=\"top\">");
            write( pBufWrite, "<a href=\"" + lLocalParam.cSrcDir  + "/" +lEntry[i]
		     +"\"><img src=\""+lDestDirName+ "/" + lDestFileName  + "\" alt=\"" + lEntry[i] +"\" border=\"0\"");
 


		write( pBufWrite, " height=\"" + lSizeThumb.getY() +"\" width=\"" + lSizeThumb.getX());
	    
	    write( pBufWrite, "\"><br>");

	    if( lLocalParam.cFlagName  )
		write( pBufWrite,	lEntry[i] );

	    writeln( pBufWrite,"</a>" );

	    if( lLocalParam.cFlagSize ){
		write( pBufWrite,	"<br>" + (lFileSrc.length()/1000) + " Ko");
	    }
	    if( lLocalParam.cFlagWH ){
		write( pBufWrite, "<br>" + ((int)lSizeImg.getX()) +"x"+((int)lSizeImg.getY()));
	    }

	    writeln(  pBufWrite, "</td>" );
	    
	    if( lColumns++ >= lLocalParam.cColumns ){
		lColumns = 0;
		//System.out.println("doFile:" + pPath + " " + pFileSrc );

		writeln(  pBufWrite, "     </tr>");
	    }
	    
	}
	
	if( lColumns != 0 )
	    writeln( pBufWrite, "     </tr>");
	
	writeln( pBufWrite, "     </tbody>");
	writeln( pBufWrite, "     </table>");
	writeln( pBufWrite, "</center>" );


	writeln( pBufWrite, "<!-- fin generation des thumbails par GenTabImgHtml (Philippe Poupon)-->");	

	// finish table declaration
	
	return true;
    }
    
    //------------------------------------------------
    //------------------------------------------------
    //------------------------------------------------
    
   
    
    // il faut un fichier commencant par thg_ et .html
    
    public void doFile( String pPath, File pFileSrc, Parameter pParam  ){
	
	// Identification du type de fichier
	//----------------------------------
      
	System.out.println("doFile:" + pPath + " " + pFileSrc );
	
	String lStrFileName = pFileSrc.getName();			
	
	int lIndexExtension = lStrFileName.lastIndexOf(".");
	if( lIndexExtension == -1 ) {
	    return ;
	}
	
	String lExtension = lStrFileName.substring( lIndexExtension+1 );
	String lStrName   = lStrFileName.substring( 0, lIndexExtension );
	
	if( lStrName.startsWith( sTypeFilePrefix  ) == false   // NEW NEW NEW 
	    || lExtension.compareTo(sFilterExtension) != 0 ) {	

	    // pas un thg_*.html : on ne fait rien !
	    return ;
	}
	
	
	// C'est un bon fichier !
	// On va le lire et ecrire un fichier *.html en generant le code et les miniatures
	System.out.println("doFile:" + pPath + " " + pFileSrc );

	try {
	    FileReader lFread = new FileReader( pFileSrc );						
	    
	    String     lNewFile = pPath + "/" + lStrName.substring( 4 )+"."+lExtension;
	    FileWriter lFwrite = new FileWriter( lNewFile );
	    
	    BufferedReader lBufRead  = new BufferedReader(lFread);
	    BufferedWriter lBufWrite = new BufferedWriter(lFwrite);
	    
	    
	    doFile( pPath, pFileSrc, pParam, lBufRead, lBufWrite );
	    
	    lBufRead.close();						
	    lBufWrite.close();

	    /*
 
    // IL SERAIT PEUT ETRE INTERESSANT D'ECRIRE DANS LE FICHIER ORIGINEL 
    // DANS CE CAS IL FAUDRAIT AVOIR LA ZONE GENERER JUSTE APRES LA COMMANDE
    // ET ENCADRER PAR DES BALISES !(ON REMPLACERER LA ZOME GENERE)
    // ==> Pb pour les includes !!!!

    	     pFileSrc.delete();

	     File lFileTmp = new File( lNewFile );
	     lFileTmp.renameTo( pFileSrc );
	    */		
	    
	}
	catch( Exception e){
	    System.err.println("catch " + e + " in PPIniFile.exec read file " );
	    e.printStackTrace();
	}				
	
    }	
    
    //------------------------------------------------
    // Parse un fichier a la recherche d'une  commande TAB
    
    public void doFile( String pPath, File pFileSrc, Parameter pParam, 
			BufferedReader pBufRead,  BufferedWriter pBufWrite ) throws IOException {
	int lNumLine = 0;
	String lSbuf;

	System.out.println( "doFile:" + pPath + "   >" + pFileSrc );
		    
	while( (lSbuf=pBufRead.readLine()) != null) {
	    lNumLine++;
			
	    if(  lSbuf.length() == 0 )
		continue;							 
	    lSbuf =lSbuf.trim();
	    System.out.println( "line:\t"+lNumLine+"\t[" +lSbuf+"]" );
	    
	    // Detection d'une ligne de commande
	    if( lSbuf.startsWith( sSign ) ){
		System.out.println("Ok");
		System.out.println( lSbuf.substring( +sSign.length(), lSbuf.indexOf(' ') ));
		
		// Recherche du type de la commande
		Command lCom = Command.sHashCommande.get( lSbuf.substring( +sSign.length(), lSbuf.indexOf(' ') ));
		
		if( lCom != null ){
		    int lPos = lSbuf.indexOf( Command.CMD_END.cStrCmd );
		    if( lPos == -1 ){
			// il n' y a pas de end !!!
			System.err.println( "Error in file " + pFileSrc + " at line " + lNumLine  + " no end found to command");
			return;
		    }						
		    
		    lSbuf = lSbuf.substring( lCom.cStrCmd.length()+sSign.length()+1, lPos ).trim();
		    
		    switch( lCom ){
			
		    case 	CMD_TAB_THUMB:     // demande de creation de miniature
			makeTab( pPath, lSbuf, pBufWrite, pParam);
			break;		
								
		    case 	CMD_INCLUDE:	  // un include			
			File lFileInclude = new File( pPath + "/" +lSbuf );
			
			if( lFileInclude.canRead() == false ){
			    System.err.println( "Error at " +  pFileSrc.getName()+ " line " + lNumLine 
						+ "include can read file:" + lSbuf );
			    continue;
			}
			
			FileReader lFread = new FileReader( lFileInclude );											 						
			BufferedReader lBufReadInc  = new BufferedReader(lFread);

			// on va lire le fichier inclus et le recopier dans la destination
			// en interpretant les commandes TAB si on les trouve

			doFile( pPath, lFileInclude, pParam, lBufReadInc, pBufWrite);
			lBufReadInc.close();						
			break;
			
		    case 	CMD_END:
			break;
		    }
		}
	    }
	    else {
		writeln( pBufWrite,  lSbuf );							
	    }										
	}
    }
    //---------------------------------
    //---------------------------------
    //---------------------------------
    // Traitement recusif de toute l'arborescence 
    // quand on rencontre un fichier de config 
    // il remplace la config courante pour le sous arborence
    
    boolean process( String pParentPath, String pPath, Parameter pParam ) {
	
	
	System.out.println("process path:" + pPath );
	
	File lFile = new File( pPath );
	
	if( lFile.isDirectory() == true ){
	    
	    // Lire s'il a lieu le fichier de parametre local qui sera valide pour l'arbre
	    Parameter lLocalParam = new Parameter( pParam );
	    
	    String lEntry[] = lFile.list( );
	    
	    // pour toutes les entrees du repertoire courant
	    for( int i=0; i< lEntry.length; i++) {								
		String lPath = pPath + "/" + lEntry[i];
		process( pPath , lPath, lLocalParam );
	    }
			
	}
	else {
	    // c'est un fichier ! on le traite
	    doFile( pParentPath, lFile, pParam );
	    return true;
	}
	
	//				System.out.println( "==========> " + pPath );
	
	//System.out.println( ">" + pDecal + lFile.getName() );
	
		    
	return true;
    }
    
    
    //---------------------------------
    //---------------------------------
    // ---   MAIN
    //---------------------------------
    //---------------------------------
    static String GetParamString( String[] args, String p_prefix, String pDefault ){
	
	int l = p_prefix.length();
	
	for( int i=0; i<  args.length; i++){
	    
	    String arg = args[i];
	    
	    if( arg.startsWith( p_prefix ))
		{
		    return arg.substring( l+1 );
		}
				}
	return pDefault;
    }
    //---------------------------------
    
    static Integer GetParamInt( String[] args, String p_prefix, Integer pDefault){
	
	int l = p_prefix.length();
	
	for( int i=0; i<  args.length; i++){
	    
	    String arg = args[i];
	    
	    if( arg.startsWith( p_prefix ))
		{
		    try{
			return new Integer( arg.substring(l+1));
		    }catch(NumberFormatException ex){
			System.err.println( "Mauvais format pour commande "+p_prefix);
			return null;
		    }					
		}
	}
	return pDefault;
    }
    //---------------------------------
    static boolean GetParam( String[] args, String p_prefix ){
	
	int l = p_prefix.length();
	
	for( int i=0; i<  args.length; i++){
	    
	    String arg = args[i];
	    
	    if( arg.startsWith( p_prefix ))
		return true;
	}
				return false;
    }		
    //---------------------------------
    static void Help( int pExit ){
	System.out.println("GenTabFileHtml v1.0 : Philippe Poupon : 2005 ");
	System.out.println("Usage:");
	System.out.println("\t-InputPath");

	System.out.println("\t-OutputPath");
	System.out.println("\t-ThumbPrefix=thumb_");
	System.out.println("\t-PrefixIn=thg_");
	System.out.println("\t-Extension=.html");
	System.out.println("\t-Eco(ne cree pas les images)");


	System.out.println("\t-Format=jpg/png)");

	System.exit( pExit );
    }
    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------
    
    public static void main(String[] args) {
	
	GenTabImgHtml	 lGenTab = new GenTabImgHtml();
	String lPath  =  GetParamString( args, "-InputPath", null );

	GenTabImgHtml.sTypeFilePrefix = GetParamString( args, "-PrefixIn", GenTabImgHtml.sTypeFilePrefix );

	GenTabImgHtml.sEco = GetParam( args, "-Eco" );	

	GenTabImgHtml.sOutputPath = GetParamString( args, "-OutputPath", null  );

	GenTabImgHtml.sFilterExtension = GetParamString( args, "-Extension", GenTabImgHtml.sFilterExtension );

	if( lPath == null || lPath.length() == 0 )
	    lPath = ".";

	Parameter lParam = new Parameter();
	lParam.cDestExt = GetParamString( args, "-ThumbPrefix", lParam.cDestExt  );

	lParam.cFormat = GetParamString( args, "-Format", lParam.cFormat  );

	lGenTab.process( lPath, lPath, lParam  );
	
	//				lGenTab.createMiniature( lSrc, 128, 128, 0, "test.jpg" );
    }
}



