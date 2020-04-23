package at.jku.isse.gitecco.translation;

import at.jku.isse.gitecco.core.git.Change;
import at.jku.isse.gitecco.core.git.GitCommit;
import at.jku.isse.gitecco.core.git.GitCommitList;
import at.jku.isse.gitecco.core.git.GitHelper;
import at.jku.isse.gitecco.core.preprocessor.PreprocessorHelper;
import at.jku.isse.gitecco.core.tree.nodes.BaseNode;
import at.jku.isse.gitecco.core.tree.nodes.ConditionalNode;
import at.jku.isse.gitecco.core.tree.nodes.FileNode;
import at.jku.isse.gitecco.core.tree.nodes.SourceFileNode;
import at.jku.isse.gitecco.core.type.Feature;
import at.jku.isse.gitecco.translation.constraintcomputation.util.ConstraintComputer;
import at.jku.isse.gitecco.translation.visitor.GetNodesForChangeVisitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.glassfish.grizzly.http.server.accesslog.FileAppender;
import scala.util.parsing.combinator.testing.Str;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class App {


    public static void main(String... args) throws Exception {
        final int MAXCOMMITS = 200;
        //set as true to generate random variants or false to just generate original variants
        final boolean generateRandomVariants = false;
        //set as true to generate PP variants or false to just generate configurations to generate random variants
        final boolean generateOriginalVariants = false;
        //TODO: planned arguments: DEBUG, dispose tree, max commits, repo path, csv path(feature id), outpath for ecco
        //String repoPath = "C:\\Users\\gabil\\Desktop\\ECCO_Work\\spls\\spls\\bug-fixed\\Random_Variants\\Marlin\\Marlin";
        String repoPath = "C:\\Users\\gabil\\Desktop\\PHD\\Mining\\systems\\Bison\\bison";
        String[] featuresToAdd = {"BASE","TRACE","DEBUG","MSDOS","eta10","__GO32__","DONTDEF","VMS","HAVE_ALLOCA_H","__GNUC__","_AIX","__STDC__","HAVE_STDLIB_H","HAVE_MEMORY_H","STDC_HEADERS"};
        /*String[] featuresToAdd = {"BASE","PKZIP_BUG_WORKAROUND","__TURBOC__","NO_STDLIB_H","isgraph","lint","S_IFMPB","NO_STRING_H","__STDC__","MACOS",
        "FEATURE_RECURSIVE","BB_BLOCK_DEVICE","S_IFIFO","MPW","atarist","TOPS20","NDIR","__OS2__","NO_ASM","FULL_SEARCH","S_IFCHR","SIGHUP","MEDIUM_MEM","S_IFNWK",
        "S_IFDIR","__GLIBC__","DEBUG","S_IFREG","__ZTC__","unix","SYSNDIR","UNALIGNED_OK","VMS","__GNU_LIBRARY__","pyr","__EMX__","__cplusplus","isblank","__GNUC__",
        "_MSC_VER","S_IFBLK","DUMP_BL_TREE","__50SERIES","WIN32","__MSDOS__","VAXC","NTFAT","TOSFS","S_IFSOCK","AMIGA","OS2FAT","NO_TIME_H","__BORLANDC__","FORCE_METHOD","ATARI"};
        //String repoPath = "C:\\Users\\gabil\\Desktop\\ECCO_Work\\spls\\spls\\libssh-mirror\\libssh-mirror";
        //String repoPath = "C:\\Users\\gabil\\Desktop\\ECCO_Work\\spls\\spls\\sqllite\\sqlite";
        //optional features of the project obtained by the featureID (chosen that which is in almost cases external feature)
        //Marlin 103 commits = 160 features
        /*String[] featuresToAdd = {"BASE","F_FILE_DIR_DIRTY","F_UNUSED","F_FILE_UNBUFFERED_READ","__AVR_ATmega644P__","__AVR_ATmega644PA__","RAMPS_V_1_0","__AVR_ATmega2560__","F_CPU","F_OFLAG","__AVR_AT90USB1286__",
                "WATCHPERIOD","THERMISTORBED","THERMISTORHEATER","PID_DEBUG","HEATER_USES_THERMISTOR","__AVR_ATmega328P__","__AVR_ATmega1280__","__AVR_ATmega168__","ADVANCE","PID_OPENLOOP",
                "__AVR_ATmega32U4__","__AVR_ATmega328__","__AVR_ATmega644__","BASE","__AVR_AT90USB1287__","BED_USES_THERMISTOR","__AVR_AT90USB646__","BED_MINTEMP","SIMPLE_LCD","DEBUG_STEPS",
                "BED_USES_AD595","ARDUINO","BED_MAXTEMP","HEATER_1_USES_THERMISTOR","THERMISTORHEATER_2","THERMISTORHEATER_1","HEATER_2_USES_THERMISTOR","HEATER_USES_THERMISTOR_1",
                "HEATER_2_USES_AD595","HEATER_1_MAXTEMP","THERMISTORHEATER_0","HEATER_1_MINTEMP","HEATER_0_USES_THERMISTOR","RESET_MANUAL","AUTOTEMP","TIMSK3","TIMSK4","TIMSK5",
                "COM3C1","MCUCR","COM3B1","ISC00","UBRR2H","OCIE5A","WGM50","USBCON","TIMSK","COM2B1","WGM42","OCR5A","COM2A1","WGM40","UBRR3H","INT0","INT1","TCCR2","TCCR1","TCCR0",
                "UBRR0H","COM4B1","__AVR_ATmega128__","WGM30","UCSRB","USART2_RX_vect","TCNT0L","UBRR0L","USART_RX_vect","SIG_UART0_RECV","COM3A1","WGM20","COM4C1","ISC11","UBRR1H",
                "ISC10","MUX5","OCIE0A","SIG_USART1_RECV","CS01","CS00","WGM10","COM5B1","COM00","WGM12","TOIE0","OCIE1A","USART3_RX_vect","COM21","TIFR0","CS11","COM5C1","CS10","UBRRL",
                "COM4A1","WGM01","__AVR_ATmega324P__","UBRRH","__AVR_ATmega8__","UCSR0B","ADABOOT","TCNT0","TCCR1B","TCCR1A","USART0_RX_vect","CS22","GICR","TCCR0A","TCCR0B","SIG_USART0_RECV",
                "CS31","COM5A1","TIMSK0","TCCR3B","TCCR3A","CS43","CS41","USART1_RX_vect","UDR3","UDR2","UDR0","CS51","EICRB","EICRA","TCCR5B","TCCR5A","GIMSK","TCCR4A","TCCR4B","ADMUX","EIMSK",
                "COM0A1","ADCSRA","SIG_USART2_RECV","ADCSRB","COM0B1","OCR1A","RAMEND","__cplusplus","UDR","COM1A1","OCR0A","SIG_UART_RECV","COM1B1","SIG_USART3_RECV",
                "ADCL","SPR0","SPR1","__AVR_ATmega1284P__","__AVR_ATmega168P__","DOXYGEN","DIDR2"};
        //Marlin 50 commits = 45 features*/
       /* String[] featuresToAdd = {"F_FILE_DIR_DIRTY", "F_UNUSED", "F_FILE_UNBUFFERED_READ", "__AVR_ATmega644P__", "__AVR_ATmega644PA__", "RAMPS_V_1_0", "__AVR_ATmega2560__", "F_CPU", "F_OFLAG",
                "__AVR_AT90USB1286__", "WATCHPERIOD", "THERMISTORBED", "THERMISTORHEATER", "PID_DEBUG", "HEATER_USES_THERMISTOR", "__AVR_ATmega328P__", "__AVR_ATmega1280__",
                "__AVR_ATmega168__", "ADVANCE", "PID_OPENLOOP", "__AVR_ATmega32U4__", "__AVR_ATmega328__", "__AVR_ATmega644__", "BASE", "__AVR_AT90USB1287__", "BED_USES_THERMISTOR",
                "__AVR_AT90USB646__", "SIMPLE_LCD", "NEWPANEL", "DEBUG_STEPS", "BED_USES_AD595", "ARDUINO", "HEATER_1_USES_THERMISTOR", "THERMISTORHEATER_2", "THERMISTORHEATER_1",
                "HEATER_2_USES_THERMISTOR", "HEATER_USES_THERMISTOR_1", "HEATER_2_USES_AD595", "HEATER_1_MAXTEMP", "THERMISTORHEATER_0", "HEATER_1_MINTEMP", "HEATER_0_USES_THERMISTOR", "RESET_MANUAL", "PID_PID", "AUTOTEMP"};
        //Marlin 40 commits = 43 features
        /*String[] featuresToAdd = {"BASE","PID_PID","RESET_MANUAL","HEATER_0_USES_THERMISTOR","HEATER_1_MINTEMP","THERMISTORHEATER_0","HEATER_1_MAXTEMP",
                "HEATER_2_USES_AD595","HEATER_USES_THERMISTOR_1","HEATER_2_USES_THERMISTOR","THERMISTORHEATER_1","THERMISTORHEATER_2","HEATER_1_USES_THERMISTOR",
                "ARDUINO","BED_USES_AD595","DEBUG_STEPS","NEWPANEL","SIMPLE_LCD","__AVR_AT90USB646__","BED_USES_THERMISTOR","__AVR_AT90USB1287__","__AVR_ATmega644__",
                "__AVR_ATmega328__","__AVR_ATmega32U4__","PID_OPENLOOP","ADVANCE","__AVR_ATmega168__","__AVR_ATmega1280__", "__AVR_ATmega328P__", "HEATER_USES_THERMISTOR",
                "PID_DEBUG","THERMISTORHEATER","THERMISTORBED","WATCHPERIOD","__AVR_AT90USB1286__","F_OFLAG","F_CPU","__AVR_ATmega2560__","RAMPS_V_1_0","__AVR_ATmega644PA__",
                "__AVR_ATmega644P__","F_FILE_UNBUFFERED_READ","F_UNUSED","F_FILE_DIR_DIRTY"};
                /*String[] featuresToAdd = {"BASE", "__AVR_ATmega644P__", "F_FILE_DIR_DIRTY", "F_UNUSED", "F_FILE_UNBUFFERED_READ", "__AVR_ATmega644PA__", "RAMPS_V_1_0", "__AVR_ATmega2560__", "F_CPU", "__AVR_AT90USB1286__", "F_OFLAG", "WATCHPERIOD",
                         "THERMISTORHEATER", "THERMISTORBED", "TSd2PinMap_hHERMISTORHEATER", "PID_DEBUG", "HEATER_USES_THERMISTOR", "__AVR_ATmega328P__", "__AVR_ATmega1280__", "__AVR_ATmega168__","BED_MINTEMP","SIMPLE_LCD","BED_MAXTEMP","HEATER_1_USES_AD595",
                         "ADVANCE", "PID_OPENLOOP","__AVR_ATmega32U4__", "__AVR_ATmega328__", "__AVR_ATmega644__","__AVR_AT90USB1287__","SDSUPPORT", "BED_USES_THERMISTOR","__AVR_AT90USB646__", "NEWPANEL", "DEBUG_STEPS", "BED_USES_AD595", "ARDUINO",
                         "HEATER_1_USES_THERMISTOR", "THERMISTORHEATER_2","THERMISTORHEATER_1", "HEATER_2_USES_THERMISTOR","HEATER_USES_THERMISTOR_1", "HEATER_2_USES_AD595", "HEATER_1_MAXTEMP", "THERMISTORHEATER_0",
                         "HEATER_1_MINTEMP", "HEATER_0_USES_THERMISTOR", "RESET_MANUAL","PID_PI","AUTOTEMP","XY_FREQUENCY_LIMIT","TIMSK3","TIMSK4","TIMSK5","COM3C1","MCUCR","COM3B1","ISC00","UBRR2H","OCIE5A","WGM50","USBCON","TIMSK","COM2B1","WGM42",
                        "OCR5A","COM2A1","WGM40","UBRR3H","INT0","INT1","TCCR2","TCCR1","TCCR0","UBRR0H","COM4B1","__AVR_ATmega128__","WGM30","UCSRB","USART2_RX_vect","TCNT0L","UBRR0L","USART_RX_vect","SIG_UART0_RECV","COM3A1","WGM20","COM4C1","ISC11",
                        "UBRR1H","ISC10","MUX5","OCIE0A","SIG_USART1_RECV","CS01","CS00","WGM10","COM5B1","COM00","WGM12","TOIE0","OCIE1A","USART3_RX_vect","COM21","TIFR0","CS11","COM5C1","CS10","UBRRL","COM4A1","WGM01","__AVR_ATmega324P__","UBRRH",
                        "__AVR_ATmega8__","UCSR0B","ADABOOT","TCNT0","TCCR1B","TCCR1A","USART0_RX_vect","CS22","GICR","TCCR0A","TCCR0B","SIG_USART0_RECV","CS31","COM5A1","TIMSK0","TCCR3B","TCCR3A","CS43","CS41","USART1_RX_vect","UDR3","UDR2","UDR0",
                        "CS51","EICRB","EICRA","TCCR5B","TCCR5A","GIMSK","TCCR4A","TCCR4B","ADMUX","EIMSK","COM0A1","ADCSRA","SIG_USART2_RECV","ADCSRB","COM0B1","OCR1A","RAMEND","__cplusplus","UDR","COM1A1","OCR0A","SIG_UART_RECV","COM1B1","SIG_USART3_RECV",
                        "ADCL","SPR0","SPR1","__AVR_ATmega1284P__","__AVR_ATmega168P__","DOXYGEN","DIDR2","E2_STEP_PIN","HEATER_2_MAXTEMP","E2_DIR_PIN","E2_ENABLE_PIN","HEATER_2_MINTEMP","BED_LIMIT_SWITCHING"};
         //LIB SSH 100 or 50 Commits = 53 features*/
       /*String[] featuresToAdd = {"WITH_SERVER", "HAVE_LIBZ", "WORDS_BIGENDIAN", "DEBUG_CRYPTO", "HAVE_OPENSSL_AES_H", "HAVE_GETHOSTBYNAME",
                "OPENSSL_VERSION_NUMBER", "HAVE_SYS_POLL_H", "HAVE_OPENSSL_BLOWFISH_H", "HAVE_SYS_TIME_H", "BASE", "HAVE_POLL",
                "HAVE_SELECT", "HAVE_GETHOSTBYADDR", "__cplusplus", "HAVE_SSH1", "NO_SERVER", "HAVE_PTY_H", "HAVE_STDINT_H", "HAVE_MEMORY_H",
                "HAVE_LIBWSOCK32", "HAVE_GETPWUID", "DEBUG", "HAVE_ERRNO_H", "HAVE_CTYPE_H", "HAVE_NETINET_IN_H", "__CYGWIN__", "HAVE_STRSEP",
                "HAVE_GETUID", "HAVE_STDIO_H", "HAVE_CONFIG_H", "HAVE_STRING_H", "HAVE_ARPA_INET_H", "HAVE_STRINGS_H", "HAVE_SYS_SOCKET_H",
                "HAVE_SYS_TYPES_H", "HAVE_STRTOLL", "HAVE_PWD_H", "HAVE_FCNTL_H", "HAVE_OPENNET_H", "TIME_WITH_SYS_TIME", "HAVE_DIRENT_H",
                "HAVE_NETDB_H", "__WIN32__", "HAVE_INTTYPES_H", "HAVE_LIBOPENNET", "HAVE_SYS_STAT_H", "__MINGW32__", "HAVE_PAM_PAM_APPL_H",
                "HAVE_SECURITY_PAM_APPL_H", "HAVE_LIBGCRYPT", "HAVE_OPENSSL_DES_H", "HAVE_LIBCRYPTO", "GCRYPT"};
        //SQLITE 100 or 50 commits = 9  features
        String[] featuresToAdd = {"YYERRORSYMBOL", "TEST_COMPARE", "_WIN32", "WIN32", "TEST", "NDEBUG", "BASE", "NO_READLINE", "TCLSH"};*/
        ArrayList<String> featureList = new ArrayList<>();

        for (String feat : featuresToAdd) {
            featureList.add(feat);
        }

        //add directories that we need to include manually to get all the files to create a clean version because "/usr/local/include"
        // and "/usr/include")does not includes files outside the root path
        final List<String> dirFiles = new ArrayList<>();
        //dirFiles.add("C:\\Users\\gabil\\Desktop\\ECCO_Work\\spls\\spls\\tests-11-02-2020\\RandomMarlin\\Marlin");
        final GitHelper gitHelper = new GitHelper(repoPath, dirFiles);
        final GitCommitList commitList = new GitCommitList(gitHelper);


        final File gitFolder = new File(gitHelper.getPath());
        final File eccoFolder = new File(gitFolder.getParent(), "ecco");
        final File randomVariantFolder = new File(gitFolder.getParent(), "randomVariants");

        Map<Feature, Integer> featureVersions = new HashMap<>();
        final Integer[] countFeaturesChanged = {0}; //COUNT PER GIT COMMIT
        final Integer[] newFeatures = {0}; //COUNT PER GIT COMMIT

        File gitRepositoryFolder = new File(gitHelper.getPath());
        File eccoVariantsFolder = new File(gitRepositoryFolder.getParent(), "ecco");
        if (eccoVariantsFolder.exists()) GitCommitList.recursiveDelete(eccoVariantsFolder.toPath());

        String fileReportFeature = "features_report_each_project_commit.csv";
        //csv to report new features and features changed per git commit of the project
        //RQ.2 How many features changed per Git commit?
        try {
            FileWriter csvWriter = new FileWriter(gitRepositoryFolder.getParent() + File.separator + fileReportFeature);
            List<List<String>> headerRows = Arrays.asList(
                    Arrays.asList("CommitNumber", "NewFeatures", "ChangedFeatures")
            );
            for (List<String> rowData : headerRows) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // end csv to report new features and features changed

        String fileStoreConfig = "configurations.csv";
        String fileStoreRandomConfig = "randomconfigurations.csv";
        //csv to save the configurations to
        try {
            FileWriter csvWriter = new FileWriter(gitRepositoryFolder.getParent() + File.separator + fileStoreConfig);
            FileWriter csvWriterRandom = new FileWriter(gitRepositoryFolder.getParent() + File.separator + fileStoreRandomConfig);
            List<List<String>> headerRows = Arrays.asList(
                    Arrays.asList("CommitNumber", "CommitName", "Config")
            );
            for (List<String> rowData : headerRows) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
                csvWriterRandom.append(String.join(",", rowData));
                csvWriterRandom.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
            csvWriterRandom.flush();
            csvWriterRandom.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //creating runtime csv
        try {
            FileWriter csvWriter = new FileWriter(gitRepositoryFolder.getParent() + File.separator + "runtime.csv");
            List<List<String>> headerRows = Arrays.asList(
                    Arrays.asList("CommitNr", "configuration", "runtimeEccoCommit", "runtimeEccoCheckout", "runtimeCleanVersionPP", "runtimeGenerateVariantPP", "runtimeGitCommit", "runtimeGitCheckout")
            );
            for (List<String> rowData : headerRows) {
                csvWriter.append(String.join(",", rowData));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //end runtime csv

        List<String> changedFiles = new ArrayList<>();
        List<String> changedFilesNext = new ArrayList<>();
        final GitCommit[] gcPrevious = {null};
        final Boolean[] previous = {true};
        commitList.addGitCommitListener((gc, gcl) -> {
            if (gcl.size() >= MAXCOMMITS) System.exit(0);
            List<String> configurations = new ArrayList<>();
            System.out.println(gc.getCommitName() + ":");

            GetNodesForChangeVisitor visitor = new GetNodesForChangeVisitor();
            Set<ConditionalNode> changedNodes = new HashSet<>();
            Set<ConditionalNode> deletedNodes = new HashSet<>();

            if (gc.getNumber() == 0) {
                gcPrevious[0] = new GitCommit(gc.getCommitName(), gc.getNumber(), gc.getDiffCommitName(), gc.getBranch(), gc.getRevCommit());
                gcPrevious[0].setTree(gc.getTree());
            } else if (previous[0] || previous[0] != null) {
                if (gc.getNumber() - 1 < 1) {
                    previous[0] = false;
                } else {
                    gcPrevious[0] = new GitCommit(gc.getRevCommit().getParent(0).getName(), gc.getNumber() - 1, gc.getRevCommit().getParent(0).getParent(0).getName(), gc.getBranch(), gc.getRevCommit().getParent(0));
                    gcl.addTreeParent(gcPrevious[0], gc.getCommitName());
                }
            }

            Map<Change, FileNode> changesInsert = new HashMap<>();
            //retrieve changed nodes
            for (FileNode child : gc.getTree().getChildren()) {
                if (child instanceof SourceFileNode) {
                    Change[] changes = null;
                    try {
                        changes = gitHelper.getFileDiffs(gc, child, false);
                    } catch (Exception e) {
                        System.err.println("error while executing the file diff: " + child.getFilePath());
                        e.printStackTrace();
                    }

                    for (Change change : changes) {
                        if (change.getChangeType() == null) {
                            visitor.setChange(change);
                            child.accept(visitor,null);
                            changedNodes.addAll(visitor.getchangedNodes());
                        } else {
                            changesInsert.put(change, child);
                        }
                    }
                }
                if (gc.getNumber() == 0) {
                    changedFiles.add(child.getFilePath());
                    previous[0] = false;
                } else {
                    changedFilesNext.add(child.getFilePath());
                }
            }

            if (previous[0]) {
                for (FileNode child : gcPrevious[0].getTree().getChildren()) {
                    String start = child.getFilePath().replace("arent" + File.separator, "");
                    changedFiles.add(start);
                    previous[0] = false;
                }
            }

            if (gc.getNumber() == 0 || previous[0]) {
                previous[0] = false;
            } else {
                //to retrieve changed nodes of deleted files
                if (gcPrevious[0] != null) {
                    for (String file : changedFiles) {
                        if (!changedFilesNext.contains(file)) {
                            FileNode child = gcPrevious[0].getTree().getChild(file);
                            if (child instanceof SourceFileNode) {
                                Change[] changes = null;
                                try {
                                    changes = gitHelper.getFileDiffs(gc, child, true);
                                } catch (Exception e) {
                                    System.err.println("error while executing the file diff: " + child.getFilePath());
                                    e.printStackTrace();
                                }

                                for (Change change : changes) {
                                    visitor.setChange(change);
                                    child.accept(visitor,null);
                                    deletedNodes.addAll(visitor.getchangedNodes());
                                }
                            }
                        }
                    }
                    for (Map.Entry<Change, FileNode> changeInsert : changesInsert.entrySet()) {
                        Change change = changeInsert.getKey();
                        FileNode childAux = changeInsert.getValue();
                        FileNode child = gcPrevious[0].getTree().getChild(childAux.getFilePath());
                        visitor.setChange(change);
                        child.accept(visitor,null);
                        deletedNodes.addAll(visitor.getchangedNodes());
                    }
                }
                //next is changedFiles for the next commit
                changedFiles.removeAll(changedFiles);
                changedFiles.addAll(changedFilesNext);
                changedFilesNext.removeAll(changedFilesNext);
            }


            final ConstraintComputer constraintComputer = new ConstraintComputer(featureList);
            final PreprocessorHelper pph = new PreprocessorHelper();
            Map<Feature, Integer> config;
            Set<Feature> changed;
            Set<Feature> alreadyComitted = new HashSet<>();
            Map<Integer, Map<Feature, Integer>> mapConfigToGenerateRandomVariants = new HashMap<>();
            Map<Integer, String> mapRevisionToGenerateRandomVariants = new HashMap<>();
            Map<Integer, String> mapRevisionToGenerateRandomVariantsFinal = new HashMap<>();
            Integer count = 0;

            //if there is no changed node then there must be a change in the binary files --> commit base.
            if (changedNodes.size() == 0 && gcl.size() > 1 && gc.getTree().getChildren().size() > 0)
                changedNodes.add(new BaseNode(null, 0));
            else if (changedNodes.size() == 0 && deletedNodes.size() == 0 && gc.getTree().getChildren().size() > 0) {
                changedNodes.add(new BaseNode(null, 0));
            }

            Boolean baseChanged = false;
            for (ConditionalNode nods : changedNodes) {
                if (nods instanceof BaseNode) {
                    baseChanged = true;
                }
            }
            if (!baseChanged) {
                for (ConditionalNode nods : deletedNodes) {
                    if (nods instanceof BaseNode) {
                        baseChanged = true;
                    }
                }
            }

            ArrayList<String> configsToCommit = new ArrayList<>();
            Map<Map<Feature, Integer>, String> configsToGenerateVariant = new HashMap<>();
            Feature base = new Feature("BASE");
            Long runtimeEccoCommit = Long.valueOf(0), runtimeEccoCheckout = Long.valueOf(0), runtimePPCheckoutGenerateVariant = Long.valueOf(0), timeBefore, timeAfter;
            Long runtimeGitCommit = Long.valueOf(0), runtimeGitCheckout = commitList.getRuntimePPCheckoutCleanVersion(), runtimePPCheckoutCleanVersion = commitList.getRuntimePPCheckoutCleanVersion();
            //changedNodes = changedNodes.stream().filter(x -> x.getLocalCondition().equals("__AVR_ATmega644P__ || __AVR_ATmega644__")).collect(Collectors.toSet());
            for (ConditionalNode changedNode : changedNodes) {
                //compute the config for the var gen
                config = constraintComputer.computeConfig(changedNode, gc.getTree());
                if (config != null && !config.isEmpty()) {
                    //compute the marked as changed features.
                    changed = constraintComputer.computeChangedFeatures(changedNode, config);

                    if (!changed.contains(base))
                        config.remove(base);
                    //map with the name of the feature and version and a boolean to set true when it is already incremented in the analysed commit
                    String eccoConfig = "";
                    for (Map.Entry<Feature, Integer> configFeature : config.entrySet()) {
                        int version = 0;
                        if (configFeature.getValue() != 0) {
                            if (featureVersions.containsKey(configFeature.getKey())) {
                                version = featureVersions.get(configFeature.getKey());
                            }
                            if (!alreadyComitted.contains(configFeature.getKey())) {
                                alreadyComitted.add(configFeature.getKey());
                                //for marlin: first commit is empty, thus we need the < 2. otherwise <1 should be enough.
                                if (gcl.size() < 2 || changed.contains(configFeature.getKey())) {
                                    version++;
                                    if (version == 1)
                                        newFeatures[0]++;
                                    else
                                        countFeaturesChanged[0]++;
                                }
                                featureVersions.put(configFeature.getKey(), version);
                            }
                            if (!configFeature.getKey().toString().equals("BASE"))
                                eccoConfig += "," + configFeature.getKey().toString() + "." + version;
                            else
                                eccoConfig += "," + configFeature.getKey().toString() + ".$$";
                        }
                    }
                    if (!eccoConfig.contains("BASE")) {
                        eccoConfig += "," + "BASE.$$";
                    }
                    eccoConfig = eccoConfig.replaceFirst(",", "");

                    if (configurations.contains(eccoConfig)) {
                        System.out.println("Config already used to generate a variant: " + eccoConfig);
                        //don't need to generate variant and commit it again at the same commit of the project git repository
                    } else {
                        mapConfigToGenerateRandomVariants.put(count, config);
                        mapRevisionToGenerateRandomVariants.put(count, eccoConfig);
                        count++;
                        timeBefore = System.currentTimeMillis();
                        //configuration that will be used to generate the variant of this changed node
                        configsToGenerateVariant.put(config, eccoConfig);
                        timeAfter = System.currentTimeMillis();
                        runtimePPCheckoutGenerateVariant = timeAfter - timeBefore;
                        runtimeGitCheckout += runtimePPCheckoutGenerateVariant;

                        configurations.add(eccoConfig);
                        //folder where the variant is stored
                        File variantsrc = new File(eccoFolder, eccoConfig);
                        String outputCSV = variantsrc.getParentFile().getParentFile().getAbsolutePath();
                        final Path variant_dir = Paths.get(String.valueOf(variantsrc));

                    }
                }
            }

            if (deletedNodes.size() != 0) {
                for (ConditionalNode deletedNode : deletedNodes) {
                    //compute the config for the var gen
                    config = constraintComputer.computeConfig(deletedNode, gcPrevious[0].getTree());
                    if (config != null && !config.isEmpty()) {
                        //compute the marked as changed features.
                        changed = constraintComputer.computeChangedFeatures(deletedNode, config);
                        if (!changed.contains(base))
                            config.remove(base);
                        //map with the name of the feature and version and a boolean to set true when it is already incremented in the analysed commit
                        String eccoConfig = "";
                        for (Map.Entry<Feature, Integer> configFeature : config.entrySet()) {
                            int version = 0;
                            if (configFeature.getValue() != 0) {
                                if (featureVersions.containsKey(configFeature.getKey())) {
                                    version = featureVersions.get(configFeature.getKey());
                                }
                                if (!alreadyComitted.contains(configFeature.getKey())) {
                                    alreadyComitted.add(configFeature.getKey());
                                    //for marlin: first commit is empty, thus we need the < 2. otherwise <1 should be enough.
                                    if (gcl.size() < 2 || changed.contains(configFeature.getKey())) {
                                        version++;
                                        if (version == 1)
                                            newFeatures[0]++;
                                        else
                                            countFeaturesChanged[0]++;
                                    }
                                    featureVersions.put(configFeature.getKey(), version);
                                }
                                if (!configFeature.getKey().toString().equals("BASE"))
                                    eccoConfig += "," + configFeature.getKey().toString() + "." + version;
                                else
                                    eccoConfig += "," + configFeature.getKey().toString() + ".$$";

                            }
                        }
                        if (!eccoConfig.contains("BASE")) {
                            eccoConfig += "," + "BASE.$$";
                        }
                        eccoConfig = eccoConfig.replaceFirst(",", "");

                        if (configurations.contains(eccoConfig)) {
                            System.out.println("Config already used to generate a variant: " + eccoConfig);
                            //don't need to generate variant and commit it again at the same commit of the project git repository
                        } else {
                            mapConfigToGenerateRandomVariants.put(count, config);
                            mapRevisionToGenerateRandomVariants.put(count, eccoConfig);
                            count++;
                            timeBefore = System.currentTimeMillis();
                            //configuration that will be used to generate the variant of this changed node
                            configsToGenerateVariant.put(config, eccoConfig);
                            timeAfter = System.currentTimeMillis();
                            runtimePPCheckoutGenerateVariant = timeAfter - timeBefore;
                            runtimeGitCheckout += runtimePPCheckoutGenerateVariant;

                            configurations.add(eccoConfig);


                        }
                    }
                }
            }

            String baseVersion = "";
            for (Map.Entry<Feature, Integer> configFeature : featureVersions.entrySet()) {
                if (configFeature.getKey().equals(base))
                    baseVersion = configFeature.getValue().toString();
            }

            //generate the variant for this config
            for (Map.Entry<Map<Feature, Integer>, String> variant : configsToGenerateVariant.entrySet()) {
                String eccoConfig = variant.getValue().replace("$$", baseVersion);
                if (generateOriginalVariants) {
                    pph.generateVariants(variant.getKey(), gitFolder, eccoFolder, gitHelper.getDirFiles(), eccoConfig);
                    System.out.println("Variant generated with config: " + eccoConfig);
                }else if(generateRandomVariants){
                    for (Map.Entry<Integer,String> map:  mapRevisionToGenerateRandomVariants.entrySet()) {
                        int auxkey = map.getKey();
                        String newBase = map.getValue();
                        if(map.getValue().contains("BASE.$$")){
                            newBase = map.getValue().replace("BASE.$$", "BASE."+baseVersion);
                        }
                        mapRevisionToGenerateRandomVariantsFinal.put(auxkey,newBase);
                    }

                }
                //config that will be used to commit the variant generated with this changed node in ecco
                configsToCommit.add(eccoConfig);
            }


            //appending to the config csv
            try {

                String fileStr = gitRepositoryFolder.getParent() + File.separator + fileStoreConfig;
                FileAppender csvWriter = new FileAppender(new File(fileStr));

                for (String configs : configsToCommit) {
                    List<List<String>> headerRows = Arrays.asList(
                            Arrays.asList(Long.toString(gc.getNumber()), gc.getCommitName(), configs)
                    );
                    for (List<String> rowData : headerRows) {
                        csvWriter.append(String.join(",", rowData));
                    }
                }
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //appending to the runtime csv
            try {
                String fileStr = gitRepositoryFolder.getParent() + File.separator + "runtime.csv";
                FileAppender csvWriter = new FileAppender(new File(fileStr));

                for (String configs : configsToCommit) {
                    List<List<String>> headerRows = Arrays.asList(
                            Arrays.asList(Long.toString(gc.getNumber()), configs.replaceAll(",", "AND"), runtimeEccoCommit.toString(), runtimeEccoCheckout.toString(), runtimePPCheckoutCleanVersion.toString(), runtimePPCheckoutGenerateVariant.toString(), runtimeGitCommit.toString(), runtimeGitCheckout.toString())
                    );
                    for (List<String> rowData : headerRows) {
                        csvWriter.append(String.join(",", rowData));
                    }
                }
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //end appending to the runtime csv

            //append results to the feature report csv
            try {
                FileAppender csvAppender = new FileAppender(new File(gitRepositoryFolder.getParent() + File.separator + fileReportFeature));
                List<List<String>> contentRows = Arrays.asList(
                        Arrays.asList(Long.toString(gc.getNumber()), newFeatures[0].toString(), countFeaturesChanged[0].toString())
                );
                for (List<String> rowData : contentRows) {
                    csvAppender.append(String.join(",", rowData));
                }
                csvAppender.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //end append results to the feature report csv

            //generate random variants
            if (generateRandomVariants) {
                if (countFeaturesChanged[0] + newFeatures[0] > 0) {
                    Random random = new Random();
                    int numberConfigs = 1;
                    if (countFeaturesChanged[0] + newFeatures[0] > 1) {
                        if ((countFeaturesChanged[0] + newFeatures[0]) > 6)
                            numberConfigs = random.nextInt((countFeaturesChanged[0] + newFeatures[0]) - 1) + 6;
                        else
                            numberConfigs = random.nextInt((countFeaturesChanged[0] + newFeatures[0]) - 1) + (countFeaturesChanged[0] + newFeatures[0]);
                    }
                    Map<Feature, Integer> mapNewConfig = new HashMap<>();
                    String featurerevision = "";
                    ArrayList<Integer> positionsMap = new ArrayList<>();
                    int positionMapToSelectConfig = random.nextInt(count);
                    positionsMap.add(positionMapToSelectConfig);
                    System.out.println(mapConfigToGenerateRandomVariants.entrySet());
                    for (int i = 0; i < numberConfigs; i++) {
                        for (Map.Entry<Feature, Integer> map : mapConfigToGenerateRandomVariants.get(positionMapToSelectConfig).entrySet()) {
                            mapNewConfig.put(map.getKey(), map.getValue());
                        }
                        featurerevision += "," + mapRevisionToGenerateRandomVariantsFinal.get(positionMapToSelectConfig);
                        positionMapToSelectConfig = random.nextInt(count);
                        if (positionsMap.contains(positionMapToSelectConfig)) {
                            while (!positionsMap.contains(positionMapToSelectConfig)) {
                                positionMapToSelectConfig = random.nextInt(count);
                            }
                        } else {
                            positionsMap.add(positionMapToSelectConfig);
                        }

                    }
                    featurerevision = featurerevision.replaceFirst(",", "");
                    if(featurerevision.contains("$$")){
                        featurerevision = featurerevision.replace("$$",baseVersion);
                    }
                    if (featurerevision.contains(",")) {
                        String[] featurerevisions = featurerevision.split(",");
                        ArrayList<String> allfeatures = new ArrayList<>();
                        String featuresToVariant = "";
                        for (String featRevision : featurerevisions) {
                            if (!allfeatures.contains(featRevision)) {
                                allfeatures.add(featRevision);
                                featuresToVariant += "," + featRevision;
                            }
                        }
                        featuresToVariant = featuresToVariant.replaceFirst(",", "");
                        featurerevision = featuresToVariant;
                        pph.generateVariants(mapNewConfig, gitFolder, randomVariantFolder, dirFiles, featuresToVariant);
                    } else {
                        pph.generateVariants(mapNewConfig, gitFolder, randomVariantFolder, dirFiles, featurerevision);
                    }
                    //append random configuration to the csv file
                    try {
                        String fileStr = gitRepositoryFolder.getParent() + File.separator + fileStoreRandomConfig;
                        FileAppender csvWriter = new FileAppender(new File(fileStr));
                        List<List<String>> headerRows = Arrays.asList(
                                Arrays.asList(Long.toString(gc.getNumber()), gc.getCommitName(), featurerevision)
                        );
                        for (List<String> rowData : headerRows) {
                            csvWriter.append(String.join(",", rowData));
                        }
                        csvWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //end append random configuration
                }
            }
            //end generate random variants

            countFeaturesChanged[0] = 0;
            newFeatures[0] = 0;
           for (Map.Entry<Feature, Integer> featureRevision : featureVersions.entrySet()) {
                System.out.println(featureRevision.getKey() + "." + featureRevision.getValue());
            }
            //RQ.2 How many times one feature changed along a number of Git commits?
            if(gc.getNumber() == commitList.size()){
                for (Map.Entry<Feature, Integer> featureRevision : featureVersions.entrySet()) {
                    if(featureRevision.getValue() > 1)
                        System.out.println(featureRevision.getKey() + " Changed " + (featureRevision.getValue()-1)+" times.");
                }
            }

        });

        //set second parameter as "NULLCOMMIT" when the first commit is 0, or null when the first commit is another (when startcommit is > 0)
        //gitHelper.getEveryNthCommit(commitList, "NULLCOMMIT", 0, 50, 1);
        gitHelper.getEveryNthCommit(commitList, "NULLCOMMIT", 0, 49, 1);
        //gitHelper.getAllCommits(commitList);


    }

}
