package com.tskj.fullsearch.lucenex;

import com.alibaba.fastjson.JSONObject;
import com.ld.lucenex.base.BaseConfig;
import com.ld.lucenex.base.Constants;
import com.ld.lucenex.config.LuceneXConfig;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * @author LeonSu
 */
public class EasyStarConfig extends LuceneXConfig {
    @Override
    public void configConstant(Constants me) {
    }

    @Override
    public void configLuceneX(BaseConfig me) {
        // 存储目录 、名称、高亮、分词器、存储类
        me.add("d:/search/", "easystar", true, new PerFieldAnalyzerWrapper(new StandardAnalyzer()), EasyStarDocument.class);
//        me.add("d:/", "doc2", true, new PerFieldAnalyzerWrapper(new IKAnalyzer()), EasyStarDocument.class);
    }
}
