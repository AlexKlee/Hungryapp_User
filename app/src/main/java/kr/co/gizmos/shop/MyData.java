package kr.co.gizmos.shop;

public class MyData {
    //수정요망, 날짜 세부사항으로 받을것인지, 아니면 하나로 받을것인지.
    public String index;
    public String date;
    public String contents;
    public String rvid;
    MyData(String i, String d, String c, String r){
        index=i;
        date=d;
        contents=c;//작성자+내용
        rvid=r;
    }
}
