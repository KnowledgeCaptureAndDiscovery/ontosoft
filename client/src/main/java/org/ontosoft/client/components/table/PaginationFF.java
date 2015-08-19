package org.ontosoft.client.components.table;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.PaginationSize;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;

public class PaginationFF extends Pagination {

  int visibleItems = 5;
  
  public PaginationFF() {
    super();
  }
  
  public PaginationFF(PaginationSize size) {
    super(size);
  }
  
  public int getVisibleItems() {
    return visibleItems;
  }

  public void setVisibleItems(int visibleItems) {
    this.visibleItems = visibleItems;
  }

  public AnchorListItem addPreviousLink() {
    final AnchorListItem listItem = new AnchorListItem();
    listItem.setIcon(IconType.ANGLE_LEFT);
    insert(listItem, 0);
    return listItem;
  }

  public AnchorListItem addNextLink() {
    final AnchorListItem listItem = new AnchorListItem();
    listItem.setIcon(IconType.ANGLE_RIGHT);
    add(listItem);
    return listItem;
  }

  public AnchorListItem addPreviousBatchLink() {
    final AnchorListItem listItem = new AnchorListItem();
    listItem.setIcon(IconType.ANGLE_DOUBLE_LEFT);
    insert(listItem, 0);
    return listItem;
  }

  public AnchorListItem addNextBatchLink() {
    final AnchorListItem listItem = new AnchorListItem();
    listItem.setIcon(IconType.ANGLE_DOUBLE_RIGHT);
    add(listItem);
    return listItem;
  }
  
  private int getNextBatchPage(SimplePager pager, int pagenum) {
    int end = visibleItems * (int)(Math.floor(pagenum / visibleItems)) + visibleItems;
    if(end >= pager.getPageCount()) 
      end = pager.getPageCount();
    return end;
  }
  
  private int getPreviousBatchPage(SimplePager pager, int pagenum) {
    int start = visibleItems * (int)(Math.floor(pagenum / visibleItems));
    if(start < 0) 
      start = 0;
    return start;
  }
  
  @Override
  public void rebuild(final SimplePager pager) {
    clear();
    GWT.log("Rebuilding");
    
    if (pager.getPageCount() == 0) {
      return;
    }
    
    int current = pager.getPage();
    int start = getPreviousBatchPage(pager, current);
    int end = start + visibleItems;
    if(end > pager.getPageCount())
      end = pager.getPageCount();
    
    final AnchorListItem prev = addPreviousLink();
    prev.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        pager.previousPage();
      }
    });
    prev.setEnabled(pager.hasPreviousPage());

    final AnchorListItem prevbatch = addPreviousBatchLink();
    prevbatch.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        int newstart = getPreviousBatchPage(pager, (pager.getPage() - 1));        
        pager.setPage(newstart);
      }
    });
    
    for (int i = start; i < end; i++) {
      final int display = i + 1;
      final AnchorListItem page = new AnchorListItem(String.valueOf(display));
      page.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          pager.setPage(display - 1);
        }
      });
      if (i == pager.getPage()) {
        page.setActive(true);
      }
      add(page);
    }

    final AnchorListItem next = addNextLink();
    next.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        pager.nextPage();
      }
    });
    next.setEnabled(pager.hasNextPage());
    
    final AnchorListItem nextbatch = addNextBatchLink();
    nextbatch.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        pager.setPage(getNextBatchPage(pager, (pager.getPage() + 1)));     
      }
    });
    
    int prevstart = getNextBatchPage(pager, current - 1);
    int nextstart = getNextBatchPage(pager, current + 1);
    prevbatch.setEnabled(prevstart != start);
    nextbatch.setEnabled(nextstart != start);
    
  }
}
