package lex.controllers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import lex.beans.Genre;
import lex.db.Database;

@ManagedBean(eager = true)
@ApplicationScoped

/**
 * Books Genre Controller
 */
public class GenreController implements Serializable {

    /**
     * List of Genges as an object property of the controller
     */
    private ArrayList<Genre> genreList;

    /**
     * Contstructor retrieves and places genres into this object
     */
    public GenreController() {
        fillGenresAll();
    }

    /**
     *
     */
    private void fillGenresAll() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        genreList = new ArrayList<>();
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from genre order by name");
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setName(rs.getString("name"));
                genre.setId(rs.getInt("id"));
                genreList.add(genre);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList<Genre> getGenreList() {
        return genreList;
    }
}
