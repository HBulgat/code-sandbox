package cn.bulgat.codesandbox.utils;

import cn.bulgat.codesandbox.model.vo.codesandbox.CompileMessage;
import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 用来获取京城的一些（输出）信息
 */
public class ProcessUtils {

    public static CompileMessage getCompileMessage(Process compileProcess) {
        CompileMessage compileMessage=new CompileMessage();
        String outputLine;
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=new StringBuilder();
        try{
            int exitCode= compileProcess.waitFor();
            if (exitCode==0){
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_SUCCESS);
                System.out.println("编译成功");
                bufferedReader=new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
            }else{
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_ERROR);
                System.out.println("编译失败,exitCode="+exitCode);
                bufferedReader=new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
            }
            //读取
            while ((outputLine=bufferedReader.readLine())!=null){
                stringBuilder.append(outputLine).append("\n");
            }
            bufferedReader.close();
            //删掉最后一个换汉服
            if (stringBuilder.length()>0){
                stringBuilder.delete(stringBuilder.length()-1,stringBuilder.length());
            }
            compileMessage.setMessage(stringBuilder.toString());
        }catch (InterruptedException | IOException e){
            compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_SUCCESS);
        }
        return compileMessage;
    }
}