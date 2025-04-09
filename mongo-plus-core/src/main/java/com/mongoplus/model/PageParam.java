package com.mongoplus.model;

/**
 * 分页参数,默认为1-10
 * @author JiaChaoYang
 **/
public class PageParam {

    /**
     * 当前页
    */
    private Integer pageNum;

    /**
     * 每页显示行数
    */
    private Integer pageSize;

    public Integer getPageNum() {
        return this.pageNum;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageParam)) {
            return false;
        } else {
            PageParam other = (PageParam)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$pageNum = this.getPageNum();
                Object other$pageNum = other.getPageNum();
                if (this$pageNum == null) {
                    if (other$pageNum != null) {
                        return false;
                    }
                } else if (!this$pageNum.equals(other$pageNum)) {
                    return false;
                }

                Object this$pageSize = this.getPageSize();
                Object other$pageSize = other.getPageSize();
                if (this$pageSize == null) {
                    if (other$pageSize != null) {
                        return false;
                    }
                } else if (!this$pageSize.equals(other$pageSize)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PageParam;
    }

    public int hashCode() {
        int result = 1;
        Object $pageNum = this.getPageNum();
        result = result * 59 + ($pageNum == null ? 43 : $pageNum.hashCode());
        Object $pageSize = this.getPageSize();
        result = result * 59 + ($pageSize == null ? 43 : $pageSize.hashCode());
        return result;
    }

    public String toString() {
        return "PageParam(pageNum=" + this.getPageNum() + ", pageSize=" + this.getPageSize() + ")";
    }

    public PageParam(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public PageParam() {
    }

}
