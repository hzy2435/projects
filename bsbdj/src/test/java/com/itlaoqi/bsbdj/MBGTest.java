package com.itlaoqi.bsbdj;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MBGTest {

	@Test
	public void generatorMBG() {

		List<String> warnings = new ArrayList<>();
		ConfigurationParser cp = new ConfigurationParser(warnings);
		File mbgFile = new File(MBGTest.class.getClassLoader().getResource("mbg.xml").getFile());

		try {
			
			Configuration configuration = cp.parseConfiguration(mbgFile);
			boolean overwrite = true;
			DefaultShellCallback dsc = new DefaultShellCallback(overwrite);
			
			MyBatisGenerator mbg = new MyBatisGenerator(configuration, dsc, warnings);
			mbg.generate(null);
			
		} catch (Exception e) {
			
			System.out.println("MBG 生成文件失败");
			e.printStackTrace();
			
		}

	}

}
