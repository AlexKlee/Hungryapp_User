package kr.co.gizmos.shop;

public class MyData {
    //수정요망, 날짜 세부사항으로 받을것인지, 아니면 하나로 받을것인지.
    public String index;
    public String date;
    public String contents;
    MyData(String i, String d, String c){
        index=i;
        date=d;
        contents=c;
    }
}
