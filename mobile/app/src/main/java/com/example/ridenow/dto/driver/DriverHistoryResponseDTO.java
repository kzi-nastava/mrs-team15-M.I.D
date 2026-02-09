package com.example.ridenow.dto.driver;

import java.util.List;

public class DriverHistoryResponseDTO {
    private List<RideHistoryDTO> content;
    private boolean last;
    private int totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean first;
    private int numberOfElements;
    private boolean empty;

    // Constructors
    public DriverHistoryResponseDTO() {}

    // Getters and setters
    public List<RideHistoryDTO> getContent() { return content; }
    public void setContent(List<RideHistoryDTO> content) { this.content = content; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public int getNumberOfElements() { return numberOfElements; }
    public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}
