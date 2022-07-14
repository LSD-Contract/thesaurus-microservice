package com.lsd.thesaurus.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lsd.thesaurus.model.Catalogue;
import com.lsd.thesaurus.model.DocumentType;
import com.lsd.thesaurus.model.Users;
import com.lsd.thesaurus.repository.CatalogueRepository;
import com.lsd.thesaurus.repository.UsersRepository;
import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.utilities.ILogger;

@Service
public class BusinessService {

	@Autowired
	@Value("${filesDir}") 
	public String filesDir;
	
	@Autowired
	@Value("${keyWordsCount}") 
	public Integer keyWordsCount;
	
	@Autowired private ILogger logger;
	@Autowired private CatalogueRepository catalogueRepository;
	@Autowired private UsersRepository usersRepository;
		
	@PostConstruct
	public void postInitialize() {

	}
    
	public ResponseBean echo(HttpServletRequest servletRrequest, String message) {
		
		logger.debug(1, this.getClass(), "echo entry# message:" + message);
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("echoed successfully");
			
			responseBean.setData("Hello "+ message);
		
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("Failed to echo");
			logger.error(1, this.getClass(), "echo API failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "echo exit#");
		return responseBean;
	}	
	
	/**
	 * Add a catelogue document
	 * @param servletRrequest
	 * @param catalogueId
	 * @param file
	 * @return
	 */
	public ResponseBean addCatalogueDocument(HttpServletRequest servletRrequest, int catalogueId, MultipartFile file) {
		
		logger.debug(1, this.getClass(), "addCatalogue entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Catalogue created successfully");
			
			Optional<Catalogue> catalogueOptional = catalogueRepository.findById(catalogueId);
			if (catalogueOptional == null || !catalogueOptional.isPresent()) {
				responseBean.setStatus(HttpStatus.EXPECTATION_FAILED);
				responseBean.setMessage("Invalid catalogue Id");
				return responseBean;
			}

			Catalogue catalogue = catalogueOptional.get();
			
			String sourceFileName = StringUtils.cleanPath(file.getOriginalFilename());
			String targetFileName = System.currentTimeMillis() + "";
			String extension = sourceFileName.substring(sourceFileName.lastIndexOf("."), sourceFileName.length());
			Path targetLocation = FileSystems.getDefault().getPath(filesDir, targetFileName + extension);
			Files.copy(file.getInputStream(), targetLocation);
		
			catalogue.setSavedfilename(targetFileName + extension);
			catalogue.setDocumentName(sourceFileName);
			
			// Extract key words
			if (catalogue.getDocumentTypeId() == DocumentType.DOCUMENT_TYPE_WORD) {
				String keyWords = getkeyWordsFromWordDocument(targetLocation.toString());
				catalogue.setKeyWords(keyWords);
			} else if (catalogue.getDocumentTypeId() == DocumentType.DOCUMENT_TYPE_PDF) {
				String keyWords = getkeyWordsFromPdfDocument(targetLocation.toString());
				catalogue.setKeyWords(keyWords);
			} else if (catalogue.getDocumentTypeId() == DocumentType.DOCUMENT_TYPE_TEXT) {
				String keyWords = getkeyWordsFromTextDocument(targetLocation.toString());
				catalogue.setKeyWords(keyWords);
			}
			Catalogue savedCatalogue = catalogueRepository.save(catalogue);
			
			responseBean.setData(savedCatalogue);
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("failed to add Catalogue Document");
			logger.error(1, this.getClass(), "addCatalogueDocument failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "addCatalogueDocument exit#");
		return responseBean;
	}
	

	/**
	 * download a catalogue document
	 * @param httpServletRequest
	 * @param documentName
	 * @return
	 */
	public ResponseEntity<InputStreamResource> downloadDocument (HttpServletRequest httpServletRequest,
			String documentName) {
		final HttpHeaders headers = new HttpHeaders();

		try {
			headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			headers.add(HttpHeaders.PRAGMA, "no-cache");
			headers.add(HttpHeaders.EXPIRES, "0");

			File documentToDownload = new File(filesDir + "/" + documentName);
			boolean exists = documentToDownload.exists();
			
			if (!exists) {
			    logger.error(1, getClass(), "Document "+documentName + " not found");
			    return ResponseEntity.notFound().headers(headers).build();
			}

			InputStreamResource resource = null;
			long contentLength = 0;
			contentLength = documentToDownload.length();
			resource = new InputStreamResource(new FileInputStream(documentToDownload));

			/*
			if (resource == null) {
			    return ResponseEntity.notFound().headers(headers).build();
			}
			*/
     
			return ResponseEntity.ok()
			        .headers(headers)
			        .contentLength(contentLength)
			        .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
			        .body(resource);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(1, getClass(), "Document "+documentName + " not found");
			return ResponseEntity.notFound().headers(headers).build();
		}
	}

	public ResponseBean credentials(HttpServletRequest servletRequest, String username, String password) {
		
		logger.debug(1, this.getClass(), "credentials entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Credentials fetched successfully");
			password = Base64.getEncoder().encodeToString(password.getBytes());
			Users userData = usersRepository.findByUsernameAndPassword(username, password);
			if (userData == null || !userData.isPresent()) {
				responseBean.setStatus(HttpStatus.EXPECTATION_FAILED);
				responseBean.setMessage("Invalid username or password");
				return responseBean;
			}
			
			responseBean.setData(userData);
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("failed to fetch user data");
			logger.error(1, this.getClass(), "credentials failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "credentials exit#");
		return responseBean;
	}

	public ResponseBean createCredentials(HttpServletRequest servletRequest, Users user) {
		logger.debug(1, this.getClass(), "createCredentials entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Credentials created successfully");
			
			user.setPassword(Base64.getEncoder().encodeToString(user.getPassword().getBytes()));
			Users userData = usersRepository.findByUsername(user.getUsername());
			if (userData != null && userData.isPresent()) {
				responseBean.setStatus(HttpStatus.EXPECTATION_FAILED);
				responseBean.setMessage("Username Already Exists");
				return responseBean;
			}
			user.setCreatedOn(LocalDateTime.now());
			user.setCreatedBy("System");
			Users createdUser = usersRepository.save(user);
			responseBean.setData(createdUser);
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("failed to create user data");
			logger.error(1, this.getClass(), "createCredentials failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "createCredentials exit#");
		return responseBean;
	}
	
	/**
	 * parse a file based on extension
	 * @param fileName
	 * @return
	 */
	private String getkeyWordsFromWordDocument(String fileName) {
		if (fileName.endsWith(".docx")) {
			return getkeyWordsFromWordDocumentDocx(fileName);
		} else {
			return getkeyWordsFromWordDocumentDoc(fileName);
		}
	}
	
	/**
	 * This function returns first 20 words from a microsoft word .docx document
	 * @param fileName
	 * @return keywords separated by space
	 */
	private String getkeyWordsFromWordDocumentDocx(String fileName) {

		XWPFWordExtractor xwpfWordExtractor = null;
        try {
        	XWPFDocument doc = new XWPFDocument(Files.newInputStream(Paths.get(fileName)));
            xwpfWordExtractor = new XWPFWordExtractor(doc);
            String docText = xwpfWordExtractor.getText();

            // Get the words in the document
            String[] words = docText.split("\\s+");
            if (keyWordsCount == null) {
            	keyWordsCount = 20;
            }
            StringBuffer keyWords = new StringBuffer();
            
            for (int i=0; i<Math.min(words.length, keyWordsCount); i++) {
            	keyWords.append(words[i]).append(" ");
            }
            
            return keyWords.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        	return "";
        } finally {
        	if (xwpfWordExtractor != null) {
        		try {
					xwpfWordExtractor.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}
	/**
	 * This function returns first 20 words from a microsoft word .doc document
	 * @param fileName
	 * @return
	 */
	private String getkeyWordsFromWordDocumentDoc(String fileName) {

		FileInputStream fis = null;
        try {
        	fis = new FileInputStream(fileName);
        	HWPFDocument document = new HWPFDocument(fis);
        	WordExtractor extractor = new WordExtractor(document);
        	String docText = extractor.getText();

            // Get the words in the document
            String[] words = docText.split("\\s+");
            if (keyWordsCount == null) {
            	keyWordsCount = 20;
            }
            StringBuffer keyWords = new StringBuffer();
            
            for (int i=0; i<Math.min(words.length, keyWordsCount); i++) {
            	keyWords.append(words[i]).append(" ");
            }
            
            return keyWords.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        	return "";
        } finally {
        	if (fis != null) {
        		try {
        			fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}
	
	/**
	 * This function returns first 20 words from a pdf document
	 * @param fileName
	 * @return
	 */
	private String getkeyWordsFromPdfDocument(String fileName) {
		
		PDDocument document = null;
        try  {

        	document = PDDocument.load(new File(fileName));
        	
            document.getClass();

            if (!document.isEncrypted()) {
			
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

				// split by whitespace
                String words[] = pdfFileInText.split("\\s+");
             
                if (keyWordsCount == null) {
                	keyWordsCount = 20;
                }
                StringBuffer keyWords = new StringBuffer();
                
                for (int i=0; i<Math.min(words.length, keyWordsCount); i++) {
                	keyWords.append(words[i]).append(" ");
                }
                
                return keyWords.toString();

            }

        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	try {
				document.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        return "";
	}

	/**
	 * This function returns first 20 words from a text file
	 * @param fileName
	 * @return
	 */
	private String getkeyWordsFromTextDocument(String fileName) {
		
		BufferedReader buffer = null;
		try {
			buffer = new BufferedReader(new FileReader(fileName));
			
            if (keyWordsCount == null) {
            	keyWordsCount = 20;
            }
			int wordCount = 0;
			StringBuffer keyWords = new StringBuffer();
			String line;
			while ((line = buffer.readLine()) != null && wordCount<=keyWordsCount) {
				
				// Remove all characters other than alphabets, numbers and space
				line = line.trim();
				line = line.replaceAll("[^a-zA-Z0-9\\s]", "");
                String[] words = line.split(" ");
                for (int i=0; i<Math.min(keyWordsCount-wordCount, words.length); i++) {
                	keyWords.append(words[i]).append(" ");
                }
                
                wordCount += Math.min(keyWordsCount-wordCount, words.length);
                
                if (wordCount >= keyWordsCount) {
                	break;
                }
            }
			
			return keyWords.toString();
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "";
	}

	/*
	public Resource downloadDocument(String fileName) throws Exception {
		
		logger.info(1, this.getClass(), "downloadDocument entry#");
		return loadFile(fileName);
	
	}
	
	private Resource loadFile(String fileName) throws Exception {
		File filePath = new File(filesDir, fileName);
		if(filePath.exists())
			return new FileSystemResource(filePath);
		else 
			throw new Exception("Requested file not found : "+fileName);

	}
	*/
}