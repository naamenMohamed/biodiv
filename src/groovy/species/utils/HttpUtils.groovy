package species.utils

import org.apache.commons.logging.LogFactory;

class HttpUtils {

	private static final log = LogFactory.getLog(this);
	
	static File download(String address, File directory, boolean forceDownload)
	{
		def file = new File(directory, address.tokenize("/")[-1]);
		if(file.exists() && !forceDownload) {
			log.debug "File already exists : "+file.getAbsolutePath();
			return file;
		}
		
		log.debug "Downloading from : "+address
		def fileOpStream = new FileOutputStream(file)
		def out = new BufferedOutputStream(fileOpStream)
		out << new URL(address).openStream()
		out.close()
		log.debug "Saving to : "+file.getAbsolutePath();
		return file;
	}
}
