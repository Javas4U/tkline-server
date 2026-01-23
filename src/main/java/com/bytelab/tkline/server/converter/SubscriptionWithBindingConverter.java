package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.vo.SubscriptionWithBindingVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionWithBindingConverter {

    SubscriptionDTO toDTO(SubscriptionWithBindingVO vo);

    List<SubscriptionDTO> toDTOList(List<SubscriptionWithBindingVO> voList);
}
