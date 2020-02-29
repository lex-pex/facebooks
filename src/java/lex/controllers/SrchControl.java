package lex.controllers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import lex.beans.Book;
import lex.db.Database;

@ManagedBean(eager = true)
@SessionScoped
public class SrchControl implements Serializable {

    private SearchType searchType;
    private String srchString;
    private Map<String, SearchType> srchMap = new HashMap<>();
    private ArrayList<Book> currentBooks;
    private Character[] rusLetters;
    private final int displayedBooks = 3;
    private Integer[] pageNumbers;
    private int selectedPage = 1;
    private int selectedGenre = 0;
    private Character selectedChar;
    private String currentSql;
    private int booksSize;  // for sql displaying
 
    public SrchControl() {
        rusLettersInit();
        initDisplay();
        ResourceBundle bundle = ResourceBundle.getBundle("lex.nls.msg",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        srchMap.put(bundle.getString("author_name"), SearchType.AUTHOR);
        srchMap.put(bundle.getString("book_name"), SearchType.TITLE);
    }

    private void initDisplay() {
        currentBooks = new ArrayList<>();
        booksAll();
    }
    
    public void booksAll() {
        selectedGenre = 0;
        selectedChar = 0;
        selectedPage = 1;
        setCurrentSql("SELECT * FROM library.book order by name");
        executeSql();
    }
    
    private void executeSql() {
        StringBuilder sb = new StringBuilder(currentSql);

        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(currentSql);
            
            rs.last();
            booksSize = rs.getRow();
            fillPageNumbers();
            
            if (booksSize > displayedBooks) {
                int firstRow = (selectedPage - 1) * displayedBooks;
                sb.append(" limit ").append(firstRow).
                        append(",").append(displayedBooks);
                rs = stmt.executeQuery(sb.toString());
            } else {
                rs.beforeFirst();
            }
            
            fillCurrentBooks(rs);
            
        } catch (SQLException ex) {
            Logger.getLogger(SrchControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SrchControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void fillPageNumbers() {
        int pages = 0;
        if (booksSize % displayedBooks > 0)
            pages = 1;
        pages += booksSize / displayedBooks;
        pageNumbers = new Integer[pages];
        for(int i = 1; i <= pages; i ++)
            pageNumbers[i - 1] = i;
    }

    private void fillCurrentBooks(ResultSet rs) throws SQLException {
        currentBooks.clear();
        Book book;
        while (rs.next()) {
            book = new Book();
            book.setId(rs.getInt("id"));
            book.setName(rs.getString("name"));
            book.setPageCount(rs.getInt("page_count"));
            book.setIsbn(rs.getString("isbn"));
            book.setGenreId(rs.getInt("genre_id"));
            book.setAuthorId(rs.getInt("author_id"));
            book.setPublisherId(rs.getInt("publisher_id"));
            book.setPublisherYear(rs.getInt("publish_year"));
            book.setDescr(rs.getString("descr"));
            currentBooks.add(book);
        }
    }

    public void page() {
        currentBooks.clear();
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        int page = 1;
        String param;
        if ((param = params.get("page_num")) != null)
            page = Integer.valueOf(param.trim());
        selectedPage = page;
        executeSql();
    }

    public void byGenre() {
        selectedChar = 0;
        selectedPage = 1;
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        Integer genre_id = Integer.valueOf(params.get("genre_id"));
        selectedGenre = genre_id;
        setCurrentSql("SELECT * FROM library.book where genre_id = " + genre_id + " order by name");
        executeSql();
    }
    
    public void byLetter() {
        selectedGenre = 0;
        selectedPage = 1;
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        String letter = params.get("letter");
        selectedChar = letter.charAt(0);
        if (searchType == SearchType.AUTHOR) {
            setCurrentSql("select * from library.book inner join library.author "
                    + "on library.book.author_id = library.author.id where "
                    + "library.author.fio like '" + letter + "%'");
        } else {
            setCurrentSql("select * from library.book where name like '" +
                    letter + "%' order by name");
        }
        executeSql();
    }

    public void bySearch() {
        selectedGenre = 0;
        selectedChar = 0;
        selectedPage = 1;
        if (searchType == SearchType.AUTHOR) {
            setCurrentSql("select * from library.book inner join library.author "
                    + "on library.book.author_id = library.author.id where "
                    + "library.author.fio like '%" + srchString + "%'");
        } else {
            setCurrentSql("select * from library.book where name like '%" +
                    srchString + "%' order by name");
        }
        executeSql();
    }

    public byte[] getImage(int id) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        byte[] image = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select image from book where id=" + id);
            while (rs.next()) image = rs.getBytes("image");
        } catch (SQLException ex) {
            Logger.getLogger(Book.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Book.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return image;
    }

    public byte[] getBook(int id) {
        byte[] content = new byte[0];
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select content from book where id=" + id);
            while (rs.next()) content = (byte[]) rs.getBytes("content");
        } catch (SQLException ex) {
            Logger.getLogger(SrchControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SrchControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return content;
    }

    public Character[] getRusLetters() {
        return rusLetters;
    }

    private void rusLettersInit() {
        rusLetters = new Character[29];
        for(char i = 'А', j = 0; i <= 'Я'; i ++)
            if (i != 'Ъ' && i != 'Ы' && i != 'Ь')
                rusLetters[j ++] = i;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public Map<String, SearchType> getSrchMap() {
        return srchMap;
    }

    public void setSrchMap(Map<String, SearchType> srchMap) {
        this.srchMap = srchMap;
    }

    public ArrayList<Book> getCurrentBooks() {
        return currentBooks;
    }

    public void setCurrentBooks(ArrayList<Book> currentBooks) {
        this.currentBooks = currentBooks;
    }

    public String getSrchString() {
        return srchString;
    }

    public void setSrchString(String srchString) {
        this.srchString = srchString;
    }

    public int getBooksDisplayed() {
        return displayedBooks;
    }

    public Integer[] getPageNumbers() {
        return pageNumbers;
    }

    public int getSelectedPage() {
        return selectedPage;
    }

    public int getSelectedGenre() {
        return selectedGenre;
    }

    public Character getSelectedGhar() {
        return selectedChar;
    }

    public int getBooksSize() {
        return booksSize;
    }

    private void setCurrentSql(String currentSql) {
        this.currentSql = currentSql;
    }
}
