# get-page
web page analyze tool

一组工具, 包括下载网页文本, 构建DOM, 根据CSS selector 查询DOM元素等流程. 

典型用法:

```java
    public static void getList(String selector, String url) {
        InfoExtracter ie = new InfoExtracter();
        Downloader d = new Downloader();
        String pageContent = d.DownloadPage(url);
        DOMBuilder dbuilder = new DOMBuilder();
        TreeWalker w = dbuilder.GetWalkerByDoc( dbuilder.GetDoc(pageContent ));
        ie.BasicQueryFindLink(selector, w, url);
        
    }
```

