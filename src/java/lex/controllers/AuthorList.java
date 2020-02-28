package lex.controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import lex.beans.Author;
import lex.db.Database;

@ManagedBean
@SessionScoped
public class AuthorList {
    private ArrayList<Author> list = new ArrayList<>();
    private ArrayList<Author> getAuthors() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from author order by fio");
            Author author;
            while (rs.next()) {
                author = new Author();
                author.setId(rs.getInt("id"));
                author.setName(rs.getString("fio"));
                list.add(author);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AuthorList.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AuthorList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }
    
    public Author getAuthor(int id) {
        if (list.isEmpty()) list = getAuthors();
        for (Author a : list)
            if (a.getId() == id)
                return a;
        return null;
    }
    
    public ArrayList<Author> getAuthorList() {
        if (!list.isEmpty()) return list;
        return getAuthors();
    }
}












