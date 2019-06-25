package com.tskj.fileEntity;

import com.tskj.pdf.Pdf;
import com.tskj.pdf.Word2Pdf;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MergeFile {

    /**
    * @Description: 合并文件到PDF利用包
    * @param
    * @return
    * @author Mao
    * @date 2019/3/6 14:16
    */
    public void mergeFile(String docID, List<Map<String, Object>> fileList){
        //获取配置文件system.properties信息
        ResourceBundle systemRes = ResourceBundle.getBundle("filePath");
        //图片类型
        String imageType = systemRes.getString("imageType");
        List<String> imageTypes = Arrays.asList(imageType.split(","));
        //获取存储路径
        String mergeSavePath = systemRes.getString("mergeFilePath");
        String uploadPath = systemRes.getString("savePath");
        String wordToPdfPath = systemRes.getString("wordToPdfPath");
        //合并后文件存储路径
        String mergeFilePath = mergeSavePath + "\\" + docID;
//        System.out.println(mergeFilePath);
        File mergePath = new File(mergeSavePath);
        File mergeFile = new File(mergeFilePath);
        File wordToPdf = new File(wordToPdfPath);

        if(!mergePath.exists()){     //如果文件夹不存在，创建文件夹
            mergePath.mkdirs();
        }
        if(!wordToPdf.exists()){     //如果文件夹不存在，创建文件夹
            wordToPdf.mkdirs();
        }
        Pdf pdf = null;
        try {

            for (Map<String, Object> map : fileList){
                if (mergeFile.exists()){         //如果文件不存在，创建文件
                    try {
                        pdf = new Pdf(mergeFilePath, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    pdf = new Pdf(mergeFilePath, true);
                }
                String fileName = map.get("FILENAME").toString();        //数据库文件名
                String fileID = map.get("FILEID").toString();
                String fileType = map.get("FILETYPE").toString();
                File subFile = new File(uploadPath +"\\"+ fileID);
                if(subFile.exists()){
                    if("pdf".equals(fileType.toLowerCase())){
                        pdf.merge(subFile);
                        pdf.save();
                    } else if("doc".equals(fileType.toLowerCase()) || "docx".equals(fileType.toLowerCase())) {
                        Word2Pdf word2Pdf = new Word2Pdf();
                        word2Pdf.convert(uploadPath + "\\" + fileID, wordToPdfPath+"\\"+fileID);     //doc转换成pdf
                        pdf.merge(wordToPdfPath+"\\"+fileID);
                        pdf.save();
                        File pdfFile = new File(wordToPdfPath+"\\"+fileID);
                        if(pdfFile.exists()){
                            pdfFile.delete();    //合并完成删除生成的PDF文件
                        }
                    } else if(imageTypes.contains(fileType)){     //如果文件格式包含在设定的图片数组中，合并
                        File fullFileName = new File(uploadPath +"\\"+ fileName);
                        File retFileName = new File(uploadPath +"\\"+ fileID);
                        retFileName.renameTo(fullFileName);
                        pdf.appendImage(uploadPath +"\\"+ fileName);
                        pdf.save();
                        //修改文件名，去除拓展名
                        fullFileName.renameTo(retFileName);
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
    * @Description: 从合并文件中移出要删除的文件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 15:19
    */
    public void delFileFromMerge(String megerFile, String delFile){
        try {
            Pdf pdf = new Pdf(megerFile, false);
            pdf.removePage(delFile);
            pdf.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public static void main(String[] args) throws IOException {
        MergeFile mergeFile = new MergeFile();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("FILENAME", "aaa.png");
        map1.put("FILETYPE", "png");
        map1.put("PATH", "F:\\testPDF");
        list.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("FILENAME", "bbb.jpg");
        map2.put("FILETYPE", "jpg");
        map2.put("PATH", "F:\\testPDF");
        list.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("FILENAME", "ccc.jpeg");
        map3.put("FILETYPE", "jpeg");
        map3.put("PATH", "F:\\testPDF");
        list.add(map3);
        Map<String, Object> map5 = new HashMap<>();
        map5.put("FILENAME", "WordToPDF.pdf");
        map5.put("FILETYPE", "pdf");
        map5.put("PATH", "F:\\testPDF");
        list.add(map5);
        Map<String, Object> map4 = new HashMap<>();
        map4.put("FILENAME", "module.docx");
        map4.put("FILETYPE", "docx");
        map4.put("PATH", "F:\\testPDF");
        list.add(map4);

//        mergeFile.mergeFile("001", list);

        mergeFile.delFileFromMerge("F:\\mergeFile\\001", "F:\\testPDF\\aaa.png");

    }*/
}
