package com.spring.app.index.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class IndexDAO_imple implements IndexDAO {

	@Qualifier("sqlsession")
	private final SqlSessionTemplate sqlsession;

	
	
	
}






