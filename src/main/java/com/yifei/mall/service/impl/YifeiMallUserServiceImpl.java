
package com.yifei.mall.service.impl;

import com.yifei.mall.common.Constants;
import com.yifei.mall.common.ServiceResultEnum;
import com.yifei.mall.controller.vo.YifeiMallUserVO;
import com.yifei.mall.dao.MallUserMapper;
import com.yifei.mall.dao.YifeiMallCouponMapper;
import com.yifei.mall.dao.YifeiMallUserCouponRecordMapper;
import com.yifei.mall.entity.MallUser;
import com.yifei.mall.entity.YifeiMallUserCouponRecord;
import com.yifei.mall.entity.YifeiMallCoupon;
import com.yifei.mall.service.YifeiMallUserService;
import com.yifei.mall.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class YifeiMallUserServiceImpl implements YifeiMallUserService {

    @Autowired
    private MallUserMapper mallUserMapper;

    @Autowired
    private YifeiMallCouponMapper yifeiMallCouponMapper;

    @Autowired
    private YifeiMallUserCouponRecordMapper yifeiMallUserCouponRecordMapper;

    @Override
    public PageResult getYifeiMallUsersPage(PageQueryUtil pageUtil) {
        List<MallUser> mallUsers = mallUserMapper.findMallUserList(pageUtil);
        int total = mallUserMapper.getTotalMallUsers(pageUtil);
        return new PageResult(mallUsers, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        String passwordMD5 = MD5Util.MD5Encode(password, Constants.UTF_ENCODING);
        registerUser.setPasswordMd5(passwordMD5);
        if (mallUserMapper.insertSelective(registerUser) <= 0) {
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        // 添加注册赠券
        List<YifeiMallCoupon> yifeiMallCoupons = yifeiMallCouponMapper.selectAvailableGiveCoupon();
        for (YifeiMallCoupon yifeiMallCoupon : yifeiMallCoupons) {
            YifeiMallUserCouponRecord couponUser = new YifeiMallUserCouponRecord();
            couponUser.setUserId(registerUser.getUserId());
            couponUser.setCouponId(yifeiMallCoupon.getCouponId());
            yifeiMallUserCouponRecordMapper.insertSelective(couponUser);
        }
        return ServiceResultEnum.SUCCESS.getResult();
    }

    @Override
    public String login(String loginName, String passwordMD5, HttpSession httpSession) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMD5);
        if (user != null && httpSession != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED.getResult();
            }
            //昵称太长 影响页面展示
            if (user.getNickName() != null && user.getNickName().length() > 7) {
                String tempNickName = user.getNickName().substring(0, 7) + "..";
                user.setNickName(tempNickName);
            }
            YifeiMallUserVO yifeiMallUserVO = new YifeiMallUserVO();
            BeanUtil.copyProperties(user, yifeiMallUserVO);
            //设置购物车中的数量
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, yifeiMallUserVO);
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }

    @Override
    public YifeiMallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession) {
        YifeiMallUserVO userTemp = (YifeiMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userTemp.getUserId());
        if (userFromDB != null) {
            if (!StringUtils.isEmpty(mallUser.getNickName())) {
                userFromDB.setNickName(YifeiMallUtils.cleanString(mallUser.getNickName()));
            }
            if (!StringUtils.isEmpty(mallUser.getAddress())) {
                userFromDB.setAddress(YifeiMallUtils.cleanString(mallUser.getAddress()));
            }
            if (!StringUtils.isEmpty(mallUser.getIntroduceSign())) {
                userFromDB.setIntroduceSign(YifeiMallUtils.cleanString(mallUser.getIntroduceSign()));
            }
            if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) > 0) {
                YifeiMallUserVO yifeiMallUserVO = new YifeiMallUserVO();
                userFromDB = mallUserMapper.selectByPrimaryKey(mallUser.getUserId());
                BeanUtil.copyProperties(userFromDB, yifeiMallUserVO);
                httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, yifeiMallUserVO);
                return yifeiMallUserVO;
            }
        }
        return null;
    }

    @Override
    public Boolean lockUsers(Integer[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }
}
