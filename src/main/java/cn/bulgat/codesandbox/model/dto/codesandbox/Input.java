package cn.bulgat.codesandbox.model.dto.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Input implements Serializable {
    private String type;
    private String inputText;
    private String inputFileName;
}
