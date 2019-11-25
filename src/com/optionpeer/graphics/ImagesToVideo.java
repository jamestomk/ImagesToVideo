package com.optionpeer.graphics;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tools.ant.DirectoryScanner;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
/**
 *
 * @author Aryan Naim 
 * This class converts JPEG images to MPEG (.mp4) extension video using Xuggler library
 * examples of USAGE:
 * -r "C:/tmp/render/imgBasenam ????.jpg"  "C:/tmp/render/movieName.avi"
 * "C:/tmp/render/quay ????.jpg"  "C:/tmp/render/quay.avi"
 */
public class ImagesToVideo {

	private final String USAGE = getClass().getSimpleName() + " [-r] 'infile pattern' outputFileFullName.avi";
	private static final int DEFAULT_VIDEO_WIDTH = 1024;
	private static final int DEFAULT_VIDEO_HEIGHT = 768;
	private static final String COMBINING_FILES_MSG = "combining %d images files of pattern %s";
	private static final String SETTING_WH_MSG = "setting video width=%d and height=%d";
	private static final String START_PROC_MSG = "starting to process %d image files";
	private static final String BAD_FILE = "convertJpegFramesToMpegVideo, file path: %s does not exist!";
	private static Options options = new Options();		// command line options
	//	public Logger logger = LoggerFactory.getLogger( this.getClass() );	// TODO: should use java-style logging
	private static String imageFilenamePattern;
	private static String aviFilename;
	//fps = how many frames will be displayed per sec, default 24
    private int frameRate = 24;
    //number of millisecs each frame will be displayed for the viewer , default 1000 millisecs
    private long durationPerFrame = 33; 
    //path to output mpeg video 
    private String outputFilenamePath = "";
    //list of JPEG pictures to be converted 
    private List<String> jpegFilePathList = new ArrayList<String>();
    //list of actual images in memory to be iterated through & encoded by Xuggler 
    private List<BufferedImage> jpegFileList = new ArrayList<BufferedImage>();
	private boolean cleanUpOption;
	/**
	 * @param args
	 */
	public static void main( String[] args ) {
			(new ImagesToVideo()).run( args );
	}

	private void run( String[] args ) {
		// optional "-r" param to remove image files
		// first  req param is image file pattern format
		// second req param is the output AVI file name
		// create Options object

		addAllCmdLineOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch ( ParseException e ) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			printUsage();
		}
		if(cmd.hasOption("help")) {
			// automatically generate the help statement
			printUsage();
			System.exit(0);
		}
		if(cmd.hasOption("r")) {
			System.out.println( "will remove image files as they are combined");
			cleanUpOption = true;
		}
		
		List<String> remainingArgs = cmd.getArgList();
		
		if ( remainingArgs.size() != 2 ) {
			printUsage();
			System.exit( -1 );
		}
		int i = 0;
		String arg0 = args[i++];
		if ( arg0.equals( "-r" ) ) {
			cleanUpOption = true;
			imageFilenamePattern = args[i++];
		} else {
			imageFilenamePattern = arg0;
		}
		aviFilename = args[i++];
		setOutputFilenamePath( aviFilename );
		setFrameRate( 30 );
		jpegFilePathList = getFilenames( imageFilenamePattern );
		if ( jpegFilePathList.size() == 0 )
			System.err.println( "no image files fit the pattern '" + imageFilenamePattern +"'" );
		else 
			convertJpegFramesToMpegVideo();
	}

	private void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( USAGE,  options );
	}
	
	private void addAllCmdLineOptions() {
		// add t option
		options
		.addOption( "help", "print this" )
		.addOption( "r", false, "remove image files processed")
		;
	}

	/**
	 * scan a directory for filenames matching a pattern
	 * @param fileNamePattern a regex to match all image files in directory.  They need to ordinally sort.
	 * @return
	 */
  	private List<String> getFilenames(String fileNamePattern) {
  		DirectoryScanner scanner = new DirectoryScanner();
  		scanner.setIncludes(new String[]{ fileNamePattern });
  		scanner.setCaseSensitive(false);
  		scanner.scan();
  		String[] files = scanner.getIncludedFiles();
  		System.out.println( String.format( COMBINING_FILES_MSG, files.length, fileNamePattern ) );
  		return Arrays.asList( files );
  	}
  	
	public void setDurationPerFrame(long  durationPerFrame) {
        this.durationPerFrame = Double.valueOf(Math.ceil(durationPerFrame * 1.15)).longValue();
    }

     public long getDurationPerFrame() {
        return durationPerFrame;
    }
     
    /**
     * converts JPEG images to MPEG (.mp4) extension video using Xuggler library
     */
    public void convertJpegFramesToMpegVideo() {
        System.out.println("convertJpegFramesToMpegVideo, start...");
        int height = DEFAULT_VIDEO_HEIGHT;
        int width = DEFAULT_VIDEO_WIDTH;
        File imgFile = null;
        BufferedImage img = null;	// per image temp 
        imgFile = new File(getJpegFilePathList().get(0));
        try {
        	// dimensions of first image will set the param for the whole video
			img = ImageIO.read(imgFile);
			height = img.getHeight();
			width = img.getWidth();
			System.out.println( String.format( SETTING_WH_MSG, width, height ) );
		} catch ( IOException e1 ) {
			System.out.println("problem reading image");
			e1.printStackTrace();
		}
        
        IContainer container = IContainer.make();
        IMediaWriter writer = null;
        long nextEncodeTime = getDurationPerFrame();
        writer = ToolFactory.makeWriter(getOutputFilenamePath(), container);
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, IRational.make(frameRate), width, height );

        //loop to load data from paths to BufferedImage objects
        System.out.println( String.format( START_PROC_MSG, jpegFilePathList.size() ));
        for (int i = 0; i < jpegFilePathList.size(); i++) {
            imgFile = new File(getJpegFilePathList().get(i));
            if (imgFile.exists()) {
                try {
                    img = ImageIO.read(imgFile);
                    writer.encodeVideo(0, img, nextEncodeTime, TimeUnit.MILLISECONDS);
                    nextEncodeTime = nextEncodeTime + getDurationPerFrame();
                    if ( cleanUpOption )
                    	imgFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println( String.format( BAD_FILE, imgFile.getPath() ) );
            }
        }
        writer.close();
        System.out.print( aviFilename + " is ready.  ");
        if ( cleanUpOption )
            System.out.println( jpegFilePathList.size() + " image files removed by -r cleanup option.");
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }
    public String getOutputFilenamePath() {
        return outputFilenamePath;
    }

    public void setOutputFilenamePath(String outputFilenamePath) {
        this.outputFilenamePath = outputFilenamePath;
    }

    public List<String> getJpegFilePathList() {
        return jpegFilePathList;
    }

    public void setJpegFilePathList(List<String> jpegFilePathList) {
        this.jpegFilePathList = jpegFilePathList;
    }

    public List<BufferedImage> getJpegFileList() {
        return jpegFileList;
    }

    public void setJpegFileList(List<BufferedImage> jpegFileList) {
        this.jpegFileList = jpegFileList;
    }

}
