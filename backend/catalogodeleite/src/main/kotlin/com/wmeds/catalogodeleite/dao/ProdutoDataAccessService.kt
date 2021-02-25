package com.wmeds.catalogodeleite.dao

import com.wmeds.catalogodeleite.model.Produto
import com.wmeds.catalogodeleite.model.SearchResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
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
        try {
            jdbcTemplate.update(sql, p.codigo, p.nome)
        } catch (e: DataAccessException) {
            if (e.message == null) throw e
            val reg = Regex("already exists.")

            if (reg.containsMatchIn(e.message!!)) {
                val msg = "Produto com código ${p.codigo} já cadastrado"
                throw DataIntegrityViolationException(msg)
            }
            throw e
        }
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

    private fun makeSqlWithOrder(sql: String, sort: Array<String>?, order: Array<String>?): String {
        var orderSql = ""
        if (!sort.isNullOrEmpty()) {
            val orderByBlocks: ArrayList<String> = arrayListOf()
            val columnsToSort = setOf("codigo", "nome")
            for (index in sort.indices) {
                if (!columnsToSort.contains(sort[index])) {
                    continue
                }
                columnsToSort.minus(sort[index])
                var orderByBlock = " ${sort[index]}"
                if (order != null && index < order.size) {
                    var orderVal = order[index].toUpperCase()
                    when(orderVal) {
                        "ASC", "DESC" -> orderByBlock += " ${orderVal}"
                    }
                }
                orderByBlocks.add(orderByBlock)
            }
            orderSql = "ORDER BY " + orderByBlocks.joinToString(", ")
        }
        return "$sql $orderSql"
    }

    private fun query(sql: String,
                      offset: Int?,
                      limit: Int?,
                      sort: Array<String>? = null,
                      order: Array<String>? = null,
                      @Nullable vararg args: Any
    ): Collection<Produto> {
        var sqlWithOrder = makeSqlWithOrder(sql, sort, order)
        if (offset != null && offset >= 0 && limit != null && limit > 0) {
            // Consulta com LIMIT e OFFSET
            return jdbcTemplate.query(
                "$sqlWithOrder LIMIT ? OFFSET ?",
                ProdutoRowMapper(),
                *args,
                limit,
                offset)
        }
        // Consulta sem LIMIT e OFFSET
        return jdbcTemplate.query(sqlWithOrder, ProdutoRowMapper(), *args)
    }

    override fun searchByNome(
        query: String,
        offset: Int?,
        limit: Int?,
        sort: Array<String>?,
        order: Array<String>?
    ): SearchResult<Produto> {
        // Case Insensitive (ILIKE) para busca por nome
        var sql = "SELECT COUNT(*) FROM produto WHERE nome ILIKE ?"
        // Consulta inserida pode estar em qualquer posição da String (%exemplo%)
        val likeExpr = SearchUtils.entreWildCards(query)
        val totalCount = jdbcTemplate.queryForObject(sql, Int::class.java, likeExpr)
        sql = "SELECT codigo, nome FROM produto WHERE nome ILIKE ?"
        val items = query(sql, offset, limit, sort, order, likeExpr)
        return SearchResult(items, totalCount)
    }

    override fun searchByCodigo(
        query: String,
        offset: Int?,
        limit: Int?,
        sort: Array<String>?,
        order: Array<String>?
    ): SearchResult<Produto> {
        var sql = "SELECT COUNT(*) FROM produto WHERE codigo LIKE ?"
        // Busca por codigos que comecem com o numero inserido (123%)
        val likeExpr = SearchUtils.wildCardFim(query)
        val totalCount = jdbcTemplate.queryForObject(sql, Int::class.java, likeExpr)
        sql = "SELECT codigo, nome FROM produto WHERE codigo LIKE ?"
        val items = query(sql, offset, limit, sort, order, likeExpr)
        return SearchResult(items, totalCount)
    }

    private fun queryForProduto(sql: String, @Nullable vararg args: Any): Produto {
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
            return queryForProduto(sql, *args)
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