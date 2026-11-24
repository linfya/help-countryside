package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.UserLocal;
import com.eng.domain.dto.AddressDTO;
import com.eng.domain.dto.PageDTO;
import com.eng.domain.po.Address;
import com.eng.mapper.AddressMapper;
import com.eng.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.eng.domain.PageQuery.DEFAULT_PAGE_NUM;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
    @Override
    public Result myAddress(Integer curran) {
        Long userId = UserLocal.getUser().getUserId();
        Page<Address> page = query()
                .orderByAsc("address_id")
                .eq("user_id", userId)
                .page(new Page<>(curran, DEFAULT_PAGE_NUM));
        List<Address> records = page.getRecords();
        PageDTO<Address> addressPageDTO = new PageDTO<>(page.getTotal(), page.getPages(), records);
        return Result.ok(addressPageDTO);
    }

    @Override
    public Result addAddress(AddressDTO addressDTO) {
        Long userId = UserLocal.getUser().getUserId();
        Address address = BeanUtil.copyProperties(addressDTO, Address.class);
        address.setUserId(userId);
        save(address);
        if (addressDTO.isToDefault()){
            defaultAddress(address.getAddressId(),0,1);
        }
        return Result.ok("添加成功");
    }

    @Override
    public Result uploadAddress(AddressDTO addressDTO, Long id) {
        Long userId = UserLocal.getUser().getUserId();
        lambdaUpdate()
                .set(Address::getHumanName,addressDTO.getHumanName())
                .set(Address::getPhone,addressDTO.getPhone())
                .set(Address::getUserArea,addressDTO.getUserArea())
                .set(Address::getUserAddress,addressDTO.getUserAddress())
                .eq(Address::getUserId,userId)
                .eq(Address::getAddressId,id)
                .update();
        if (addressDTO.isToDefault()){
            defaultAddress(id,0,1);
        }
        return Result.ok("修改成功");

    }

    @Override
    public Result defaultAddress(Long id,int setStatus,int eqStatus) {
            lambdaUpdate()
                .set(Address::getStatus, setStatus)
                .eq(Address::getStatus, eqStatus)
                .update();
            lambdaUpdate()
                    .set(Address::getStatus,eqStatus)
                    .eq(Address::getAddressId,id)
                    .update();
        return Result.ok("修改成功");
    }

    @Override
    public Result deleteAddress(Long id) {
        Long userId = UserLocal.getUser().getUserId();
        QueryWrapper<Address> addressQueryWrapper = new QueryWrapper<>();
        addressQueryWrapper.eq("user_id",userId).eq("address_id",id);
        boolean remove = remove(addressQueryWrapper);
        if (remove){
            return Result.ok("删除成功");
        }
        return Result.fail("删除失败");
    }
}
