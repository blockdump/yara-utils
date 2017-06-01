package yara;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guenter Porzer
 *
 */
public class MergeYaraRules
{

    private String[] m_programArguments;

    private FileWriter m_mergefile;
    private File m_scandirectory;
    private boolean m_fullpathOption;
    private boolean m_includeOption;
    private String m_rulefileExtension;

    private static final String MERGEFILE_PARAM = "mergefile";
    private static final String SCANDIRECTORY_PARAM = "scandirectory";
    private static final String USEINCLUDE_PARAM = "use_include";
    private static final String USEFULLPATH_PARAM = "use_full_path";
    private static final String RULEFILE_EXTENSION_PARAM = "rulefile_extension";

    public MergeYaraRules(String[] args)
    {
        m_programArguments = args;
        m_fullpathOption = false;
        m_includeOption = true;
        // if no filter for rulefiles is set, all files will be merged
        m_rulefileExtension = null;
    }

    private void cleanup()
    {
        try
        {
            m_mergefile.flush();
            m_mergefile.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(MergeYaraRules.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void traverseDirectory(File root)
    {
        File[] files = root.listFiles();

        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
                System.out.println("scanning: " + files[i]);
                traverseDirectory(files[i]);
            }
            else
            {
                // check for rulefile extensions filter present 
                // exclude all files not matching the extension
                if ((m_rulefileExtension != null) && (files[i].getName().endsWith(m_rulefileExtension) == false))
                {
                    continue;
                }
                else
                {
                    mergeRulefile(files[i]);
                }
            }
        }

    }

    private void mergeRulefile(File f)
    {

        if (m_includeOption == true)
        {
            includeFile(f);
        }
        else
        {
            appendFile(f);
        }
    }

    private void includeFile(File f)
    {
        try
        {
            if (m_fullpathOption == false)
            {
                Path pathAbsolute = Paths.get(f.getAbsolutePath());
                Path pathBase = Paths.get(m_scandirectory.getAbsolutePath());
                Path pathRelative = pathBase.relativize(pathAbsolute);
                System.out.println("include rulefile: " + pathRelative);
                m_mergefile.write("include \"" + pathRelative + "\"" + System.getProperty("line.separator"));
            }
            else
            {
                System.out.println("include rulefile: " + f.getAbsolutePath());
                m_mergefile.write("include \"" + f.getAbsolutePath() + "\"" + System.getProperty("line.separator"));
            }
        }
        catch (IOException ex)
        {
            System.out.println("Error includng file: " + f.getAbsolutePath());
        }
    }

    private void appendFile(File f)
    {
        System.out.println("append rulefile: " + f.getAbsolutePath());
        String fileContents = readFile(f);
        if (f != null)
        {
            try
            {
                m_mergefile.write(fileContents);
            }
            catch (IOException ex)
            {
                System.out.println("Error appending file: " + f.getAbsolutePath() + " to " + m_mergefile);
            }
        }
    }

    private String readFile(File f)
    {
        if (f.canRead() == false)
        {
            return null;
        }
        try
        {
            String contents = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
            return contents;
        }
        catch (IOException ex)
        {
            System.out.println("Error readding file: " + f.getAbsolutePath());
            return null;
        }

    }

    private void parseArguments()
    {
        if (m_programArguments.length == 1)
        {
            if (m_programArguments[0].equals("-h"))
            {
                printUsage();
                System.exit(0);
            }
        }

        for (int i = 0; i < m_programArguments.length; i++)
        {
            if (m_programArguments[i].startsWith(MERGEFILE_PARAM + "="))
            {
                File f = null;
                try
                {
                    f = new File(m_programArguments[i].substring((MERGEFILE_PARAM + "=").length()));
                    f.createNewFile();
                    m_mergefile = new FileWriter(f);
                }
                catch (IOException ex)
                {
                    System.out.println("ERROR: directory: " + f.getAbsolutePath() + " does not exist");
                }

            }
            else if (m_programArguments[i].startsWith(SCANDIRECTORY_PARAM + "="))
            {

                m_scandirectory = new File(m_programArguments[i].substring((SCANDIRECTORY_PARAM + "=").length()));
                if (m_scandirectory.isDirectory() == false)
                {
                    System.out.println("ERROR: directory: " + m_scandirectory.getAbsolutePath() + " does not exist");
                    System.exit(1);
                }
            }
            else if (m_programArguments[i].startsWith(USEINCLUDE_PARAM + "="))
            {
                if (m_programArguments[i].endsWith("yes"))
                {
                    m_includeOption = true;
                }
                else if (m_programArguments[i].endsWith("no"))
                {
                    m_includeOption = false;
                }
                else
                {
                    System.out.println("ERROR: wrong parameter for : " + USEINCLUDE_PARAM + " - only 'yes' or 'no' allowed");
                    System.exit(1);
                }
            }
            else if (m_programArguments[i].startsWith(USEFULLPATH_PARAM + "="))
            {
                if (m_programArguments[i].endsWith("yes"))
                {
                    m_fullpathOption = true;
                }
                else if (m_programArguments[i].endsWith("no"))
                {
                    m_fullpathOption = false;
                }
                else
                {
                    System.out.println("ERROR: wrong parameter for : " + USEFULLPATH_PARAM + " - only 'yes' or 'no' allowed");
                    System.exit(1);
                }
            }
            else if (m_programArguments[i].startsWith(RULEFILE_EXTENSION_PARAM + "="))
            {

                m_rulefileExtension = m_programArguments[i].substring((RULEFILE_EXTENSION_PARAM + "=").length());
            }

        }
    }

    public void start()
    {

        traverseDirectory(m_scandirectory);
    }

    public static void main(String[] args)
    {
        MergeYaraRules merger = new MergeYaraRules(args);
        merger.parseArguments();
        merger.start();
        merger.cleanup();
        System.out.println("done..");

    }

    public static void printUsage()
    {
        System.out.println("Usage:");
        System.out.println("java -jar ./yarahelper.jar MergeYaraRules mergefile=<output-file> scandirectory=<rules-file-directory> [use_include=<yes|no>] [use_full_path=<yes|no>] [rulefile_extension=<extension>]");
        System.out.println("mergefile: is the resulting yara rules file. eg. MyRules.yar");
        System.out.println("directory: is the directory where the yara rules to be merged reside.");
        System.out.println("use_include: specifies if the rule files should be included using the \"include\" syntax, or if the contents of the rules files should be copied into the resulting mergefile.");
        System.out.println("use_full_path: specifies if the \"include\" option is used , if absolute or relative paths should be used");
        System.out.println("rulefile_extension: specifies a filter for rulefiles. Files not matching the extension are not processed.If no extension is provided all files will be treated as yara rule-files");
    }

}
