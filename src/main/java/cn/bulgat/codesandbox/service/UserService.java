package cn.bulgat.codesandbox.service;

import cn.bulgat.codesandbox.model.entity.User;
import cn.bulgat.codesandbox.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bulgat.codesandbox.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author bulgat
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-01-04 18:53:29
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User user);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVO(List<User> userList);
}
