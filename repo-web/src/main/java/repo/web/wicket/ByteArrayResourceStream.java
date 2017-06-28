package repo.web.wicket;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

public class ByteArrayResourceStream extends AbstractResourceStream { 
	 
    private static final long serialVersionUID = 1L; 

    private final byte[] content; 

    public ByteArrayResourceStream(final byte[] content) { 
        this.content = content; 
    } 

    public Bytes length() { 
        return content == null? Bytes.bytes(0) : Bytes.bytes(content.length); 
    } 

    public ByteArrayInputStream getInputStream() throws ResourceStreamNotFoundException { 
        if (content == null) { 
            throw new ResourceStreamNotFoundException(); 
        } 
        return new ByteArrayInputStream(content); 
    } 

    public String getContentType() { 
        return "image/png"; 
    }

	@Override
	public void close() throws IOException {} 

} 