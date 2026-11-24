package com.eng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eng.util.Result;
import com.eng.domain.dto.AddressDTO;
import com.eng.domain.po.Address;

public interface AddressService extends IService<Address> {
    Result myAddress(Integer curran);

    Result addAddress(AddressDTO addressDTO);

    Result uploadAddress(AddressDTO addressDTO, Long id);

    Result defaultAddress(Long id,int setStatus,int eqStatus);

    Result deleteAddress(Long id);
}
