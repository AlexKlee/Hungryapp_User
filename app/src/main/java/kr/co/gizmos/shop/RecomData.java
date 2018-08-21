package kr.co.gizmos.shop;

public class RecomData {
//받기->변수 선언(가게명, 대표명, 연락처, 주소, 가게좌표x, 가게좌표y, 테이블 수, 의자최소, 의자 최대,
    //  평점, 선택 인원(수?), 가게정보업데이트 날짜, 이미지 주소, 메뉴가격, 몇인분, 1인분당 가격,메뉴설명
    //  rsl_date, mi_date,다른메뉴
    public String shopName, shopOwner, shopTel, shopAddr,  shopAddrX, shopAddrY, shopTbl ,shopChairMin, shopChairMax,
    imageUrl,choiceScore, choiceCount, shopUpdate, menuId,menuName,menuImage,menuPrice,menuPer, menuPerPrice, mi_Comment, menuUpdate,otherMenu;

    RecomData(String n, String ow, String nu, String add, String x, String y, String tb, String cmin, String cmax, String iurl,
              String cs, String cc, String su,String mi, String mn, String im, String mp, String mpe, String pp, String mc, String mu, String otm){
        shopName=n;
        shopOwner=ow;
        shopTel=nu;
        shopAddr=add;
        shopAddrX=x;
        shopAddrY=y;
        shopTbl=tb;
        shopChairMin=cmin;
        shopChairMax=cmax;
        imageUrl=iurl;
        choiceScore=cs;
        choiceCount=cc;
        shopUpdate=su;
        menuId=mi;
        menuName=mn;
        menuImage=im;
        menuPrice=mp;
        menuPer=mpe;
        menuPerPrice=pp;
        mi_Comment=mc;
        menuUpdate=mu;
        otherMenu=otm;
    }

}
