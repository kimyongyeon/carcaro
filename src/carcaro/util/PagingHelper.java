package carcaro.util;

public class PagingHelper {
	public static PagingHelper instance = new PagingHelper();
	
	private PagingHelper()
	{
	}

	public String autoPaging(long plTotalCnt, long plRowRange, long plPageRange, long plCurrPage)
	{
		StringBuffer tsRetVal = new StringBuffer();
		if (plTotalCnt == 0L) {
			return "";
		}
		
		long plPageCnt = plTotalCnt % plRowRange;
		if (plPageCnt == 0L) {
			plPageCnt = plTotalCnt / plRowRange;
		} else {
			plPageCnt = plTotalCnt / plRowRange + 1L;
		}
		
		tsRetVal.append("<table cellpadding=0 cellspacing=0 border=0>\n");
		tsRetVal.append("<tr>");
		tsRetVal.append("<td>");
		
		long plRangeCnt = plCurrPage / plPageRange;
		if (plCurrPage % plPageRange == 0L) {
			plRangeCnt = plCurrPage / plPageRange - 1L;
		}
		
		long tlFirstPage = plCurrPage - plPageRange;
		if (tlFirstPage > 0) {
			tsRetVal.append("<a href=\"javascript:goPage('1');\">");
//			tsRetVal.append("<img src=\"./images/btn_pageFirst.gif\" border=0 align=\"absmiddle\">");
			tsRetVal.append("≪");
			tsRetVal.append("</a>\n");
		} else {
//			tsRetVal.append("<img src=\"./images/btn_pageFirstR.gif\" border=0 align=\"absmiddle\">\n");
			tsRetVal.append("<font color=\"#cccccc\">≪</font>");
		}
		
		
		if (plCurrPage > plPageRange)
		{
			String s2;
			if (plCurrPage % plPageRange == 0L) {
				s2 = Long.toString(plCurrPage - plPageRange);
			} else {
				s2 = Long.toString(plCurrPage - plCurrPage % plPageRange);
			}
			tsRetVal.append("<a href=\"javascript:goPage('").append(s2).append("');\">");
//			tsRetVal.append("<img src=\"./images/btn_pagePrev.gif\" border=0 align=\"absmiddle\">");
			tsRetVal.append("<");
			tsRetVal.append("</a>\n");
		} else {
//			tsRetVal.append("<img src=\"./images/btn_pagePrevR.gif\" border=0 align=\"absmiddle\">\n");
			tsRetVal.append("<font color=\"#cccccc\"><</font>");
		}
		
		
		for (long index = plRangeCnt * plPageRange + 1L; index < (plRangeCnt + 1L) * plPageRange + 1L; index++) {
			String tsFontBegin = "<font size=2>";
			String tsFonfEnd = "</font>\n";
			
			if (index == plCurrPage) {
				tsFontBegin = "<font size=2><b>";
				tsFonfEnd = "</b></font>\n";
			}
			tsRetVal.append(tsFontBegin);
			tsRetVal.append("<a href=\"javascript:goPage('").append(Long.toString(index)).append("');\">").append(Long.toString(index)).append("</a>");
			tsRetVal.append(tsFonfEnd);
			
			if (index == plPageCnt)	break;
		}
		
		if (plPageCnt > (plRangeCnt + 1L) * plPageRange) {
			tsRetVal.append("<a href=\"javascript:goPage('").append(Long.toString((plRangeCnt + 1L) * plPageRange + 1L)).append("');\" ").append(">");
//			tsRetVal.append("<img src=\"./images/btn_pageNext.gif\" border=0 align=\"absmiddle\">");
			tsRetVal.append(">");
			tsRetVal.append("</a>\n");
		} else {
//			tsRetVal.append("<img src=\"./images/btn_pageNextR.gif\" border=0 align=\"absmiddle\"></a>\n");
			tsRetVal.append("<font color=\"#cccccc\">></font>");
		}
		
		long tlEndPage = plCurrPage + plPageRange;
		if (tlEndPage < plPageCnt) {
			tsRetVal.append("<a href=\"javascript:goPage('").append(Long.toString(plPageCnt)).append("');\" ").append(">");
//			tsRetVal.append("<img src=\"./images/btn_pageEnd.gif\" border=0 align=\"absmiddle\">");
			tsRetVal.append("≫");
			tsRetVal.append("</a>\n");
		} else {
//			tsRetVal.append("<img src=\"./images/btn_pageEndR.gif\" border=0 align=\"absmiddle\"></a>\n");
			tsRetVal.append("<font color=\"#cccccc\">≫</font>");
		}
		tsRetVal.append("</td>");
		tsRetVal.append("</tr>");
		tsRetVal.append("</table>\n");
		return tsRetVal.toString();
	}
}
