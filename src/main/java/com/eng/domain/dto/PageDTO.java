package com.eng.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class PageDTO<T> {

    /*
    * 总条数
    * */
    private Long total;

    /*
    * 总页数
    * */
    private Long page;

    /*
    * 数据集合
    * */
    private List<T> list;
}
