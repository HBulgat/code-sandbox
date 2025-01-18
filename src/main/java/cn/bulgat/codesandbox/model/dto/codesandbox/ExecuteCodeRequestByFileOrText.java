package cn.bulgat.codesandbox.model.dto.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeRequestByFileOrText implements Serializable {
    private List<Input> inputList;
    private String code;
    private String language;
}
