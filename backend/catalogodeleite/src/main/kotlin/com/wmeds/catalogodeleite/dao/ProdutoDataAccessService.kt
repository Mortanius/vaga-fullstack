package com.wmeds.catalogodeleite.dao

import com.wmeds.catalogodeleite.model.Produto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.lang.Nullable
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository("postgres")
class ProdutoDataAccessService (
    @Autowired private val jdbcTemplate: JdbcTemplate
        ): IProdutoDao {

    private class ProdutoRowMapper : RowMapper<Produto> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Produto =
            Produto(rs.getString("codigo"), rs.getString("nome"))
    }

    override fun get(codigo: String): Produto {
        val sql = "SELECT codigo, nome FROM produto WHERE codigo=?"
        try {
            return jdbcTemplate.queryForObject(sql, ProdutoRowMapper(), codigo)!!
        } catch (e: Exception) {
            when (e) {
                is EmptyResultDataAccessException, is NullPointerException -> {
                    throw NoSuchElementException("Produto com código $codigo não encontrado")
                } else -> throw e
            }
        }
    }

    override fun create(p: Produto) {
        val sql = "INSERT INTO produto (codigo, nome) VALUES (?, ?)"
        jdbcTemplate.update(sql, p.codigo, p.nome)
    }

    override fun delete(p: Produto) {
        val sql = "DELETE FROM produto WHERE codigo=?"
        jdbcTemplate.update(sql, p.codigo)
    }

    private class SearchUtils {
        companion object {
            fun entreWildCards(query: String) = "%$query%"
            fun wildCardFim(query: String) = "$query%"
        }
    }

    private fun query(sql: String,
                      offset: Int?,
                      limit: Int?,
                      @Nullable vararg args: Any
    ): Collection<Produto> {
        if (offset != null && offset >= 0 && limit != null && limit > 0) {
            // Consulta com LIMIT e OFFSET
            return jdbcTemplate.query(sql + " LIMIT ? OFFSET ?",
                ProdutoRowMapper(),
                *args,
                limit,
                offset)
        }
        // Consulta sem LIMIT e OFFSET
        return jdbcTemplate.query(sql, ProdutoRowMapper(), *args)
    }

    override fun searchByNome(query: String, offset: Int?, limit: Int?): Collection<Produto> {
        val sql = "SELECT codigo, nome FROM produto WHERE nome ILIKE ?"
        return query(sql, offset, limit, SearchUtils.entreWildCards(query))
    }

    override fun searchByCodigo(query: String, offset: Int?, limit: Int?): Collection<Produto> {
        val sql = "SELECT codigo, nome FROM produto WHERE codigo LIKE ?"
        return query(sql, offset, limit, SearchUtils.wildCardFim(query))
    }

    private fun queryForObject(sql: String, @Nullable vararg args: Any): Produto {
        try {
            return jdbcTemplate.queryForObject(sql, ProdutoRowMapper(), *args)!!
        } catch (e: Exception) {
            when (e) {
                is EmptyResultDataAccessException, is NullPointerException -> {
                    throw NoSuchElementException()
                }
                else -> throw e
            }
        }
    }

    private fun getIfSatisfiesQuery(sql: String,
                                    query: String,
                                    codigo: String,
                                    vararg args: Any
    ): Produto {
        try {
            return queryForObject(sql, *args)
        } catch (_: NoSuchElementException) {
            throw NoSuchElementException("Produto com código $codigo não encontrado na busca \"$query\"")
        }
    }

    override fun getIfSatisfiesSearchByNome(query: String, codigo: String): Produto {
        val sql = "SELECT codigo, nome FROM produto WHERE codigo=? AND nome ILIKE ?"
        return getIfSatisfiesQuery(sql, query, codigo,
            // Para Prepared Statement
            codigo, SearchUtils.entreWildCards(query))
    }

    override fun getIfSatisfiesSearchByCodigo(query: String, codigo: String): Produto {
        val sql = "SELECT codigo, nome FROM produto WHERE codigo=? AND codigo LIKE ?"
        return getIfSatisfiesQuery(sql, query, codigo,
            // Para Prepared Statement
            codigo, SearchUtils.wildCardFim(query))
    }

}