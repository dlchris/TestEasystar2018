package com.tskj.fullsearch.lucenex;

import com.alibaba.fastjson.JSONObject;
import com.ld.lucenex.core.LdService;
import com.ld.lucenex.core.LuceneX;
import com.ld.lucenex.service.BasisService;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EasyStarSearch {
    BasisService basisService;

    public EasyStarSearch(String key) {
        basisService = LdService.newInstance(BasisService.class, key);
        try {
            basisService.deleteAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(String tableName, JSONObject value) {
        List<EasyStarDocument> values = new ArrayList<>();
        try {
            for (int i = 0; i < 10; i++) {
                EasyStarDocument empty = new EasyStarDocument();
                empty.setId(i);
                empty.setName(tableName);
                empty.setText(value.toJSONString());
                values.add(empty);
            }

//            Document document = new Document();
//            Field field = new TextField("content", value.toJSONString(), Field.Store.YES);
//            document.add(field);
//            basisService.addDocument(document);
//            TextField textField = new TextField("content", value.toJSONString(), Field.Store.YES);
//            values.add(value);
            basisService.addIndex(values);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void search() {
        FuzzyQuery fuzzyQuery1 = new FuzzyQuery(new Term("text", "天深"));
        TermQuery fuzzyQuery2 = new TermQuery (new Term("name", "doc1"));
        try {
//            Directory directory = FSDirectory.open(Paths.get("d:/search/"));
//            IndexSearcher is = IndexSearcher(directory);
//            QueryParser
            BooleanQuery booleanQuery = new BooleanQuery.Builder()
                    .add(fuzzyQuery1, BooleanClause.Occur.MUST)
                    .add(fuzzyQuery2, BooleanClause.Occur.MUST)
                    .build();
//            TopDocs hits = is.search(booleanQuery, 1000);
//            System.out.println(hits.totalHits);
            List<Document> list = basisService.searchList(booleanQuery, Integer.MAX_VALUE);
            System.out.println(list.size());
            list = basisService.searchTotal();
            System.out.println(list.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LuceneX.start(EasyStarConfig.class);
        EasyStarSearch easyStarSearch = new EasyStarSearch("easystar");
        JSONObject map = JSONObject.parseObject("{}");
        map.put("DOCID", "苏鹏");
        map.put("DOCNAME", "tskj");
        map.put("DOCORG", "苏天");
        easyStarSearch.add("doc1", map);
        map.put("DOCID", "天深");
        map.put("DOCNAME", "苏");
        map.put("DOCORG", "leonts");
        easyStarSearch.add("doc2", map);
        Thread.sleep(1000);
        easyStarSearch.search();
    }
}
