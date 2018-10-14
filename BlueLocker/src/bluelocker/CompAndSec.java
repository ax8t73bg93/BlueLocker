/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluelocker;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

enum State{
    DECOMP_AND_UNSEC, COMP_AND_UNSEC, COMP_AND_SEC;
}

public class CompAndSec {
    private final File inFile;
    private final String filePath;
    private final String fileName;
    private State state;
    
    
    CompAndSec(File incomingFile, boolean isFile){
        this.inFile = incomingFile;
        this.filePath = inFile.getAbsolutePath();
        this.fileName = FilenameUtils.removeExtension(filePath);
        this.state = isFile ? State.COMP_AND_SEC : State.DECOMP_AND_UNSEC;
    }
    
    
    public int compress() {
        if(state == State.DECOMP_AND_UNSEC){
            try{
                ZipFile zipFile = new ZipFile(filePath + ".zip");
                //File inputFileH = new File(filePath);
                ZipParameters parameters = new ZipParameters();

                // COMP_DEFLATE is for compression
                // COMP_STORE no compression
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setIncludeRootFolder(false);
                // DEFLATE_LEVEL_ULTRA = maximum compression
                // DEFLATE_LEVEL_MAXIMUM
                // DEFLATE_LEVEL_NORMAL = normal compression
                // DEFLATE_LEVEL_FAST
                // DEFLATE_LEVEL_FASTEST = fastest compression
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

                 // file compressed
                zipFile.addFolder(filePath, parameters);

                //File outputFileH = new File(filePath + ".zip");

            } catch (Exception e) {
              return -1;
             }
            
            File file1 = new File(filePath);
            try {
                FileUtils.deleteDirectory(file1);
            } catch (IOException ex) {
                return -1;
            }
            state = State.COMP_AND_UNSEC;
            return 0;
        }
        return 1;
    }
    
    public int decompress() {
        if(state == State.COMP_AND_UNSEC){
            try {
               ZipFile zipFile = new ZipFile(fileName + ".zip");
               zipFile.extractAll(filePath);
            } catch (ZipException e) {
               return -1;
            }
            
            Path path = Paths.get(fileName + ".zip");
            try {
                Files.delete(path);
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n", path);
                return -1;
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n", path);
                return -1;
            } catch (IOException x) {
                // File permission problems are caught here.
                System.err.println(x);
                return -1;
            }
            state = State.DECOMP_AND_UNSEC;
            return 0;
        }
        return 1;
    }
    
    public int encrypt(String password) {
        if(state == State.COMP_AND_UNSEC){
            Runtime rt = Runtime.getRuntime();
            Process pr;
            try {
                pr = rt.exec("aescrypt -e -p " + password + " " + fileName + ".zip");
            } catch (IOException ex) {
                return -1;
            }
            try{
                if(pr.waitFor() != 0){
                    return -1;
                }           
            }catch(InterruptedException e){
                return -1;
            }

            Path path = Paths.get(fileName + ".zip");
            try {
                Files.delete(path);
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n", path);
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n", path);
            } catch (IOException x) {
                // File permission problems are caught here.
                System.err.println(x);
            }
            //filePath += ".aes";
            state = State.COMP_AND_SEC;
            return 0;
        }
        return 1;
    }
    
    public int decrypt(String password){
        if(state == State.COMP_AND_SEC){
            Runtime rt = Runtime.getRuntime();
            Process pr;
            try {
                pr = rt.exec("aescrypt -d -p " + password + " " + fileName + ".zip.aes");
            } catch (IOException ex) {
                return -1;
            }
            try{
                if(pr.waitFor() != 0){
                    return -1;
                }
            }catch(InterruptedException e){
                return -1;
            }

            Path path = Paths.get(fileName + ".zip.aes");
            try {
                Files.delete(path);
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n", path);
                return -1;
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n", path);
                return -1;
            } catch (IOException x) {
                // File permission problems are caught here.
                System.err.println(x);
                return -1;
            }
            //filePath.replaceAll(".aes", "");
            state = State.COMP_AND_UNSEC;
            return 0;
        }
        return 1;
    }    
}
