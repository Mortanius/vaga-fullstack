package com.wmeds.catalogodeleite.dao

import com.wmeds.catalogodeleite.model.Produto
import com.wmeds.catalogodeleite.model.SearchResult

interface IProdutoDao {

    fun get(codigo: String): Produto

    fun create(p: Produto)

    fun delete(p: Produto)

    // Buscam produtos que correspondem à consulta
    fun searchByNome(query: String, offset: Int? = null, limit: Int? = null): SearchResult<Produto>

    fun searchByCodigo(query: String, offset: Int? = null, limit: Int? = null): SearchResult<Produto>

    // Retorna o produto com o código informado caso ele corresponda à consulta
    fun getIfSatisfiesSearchByNome(query: String, codigo: String): Produto

    fun getIfSatisfiesSearchByCodigo(query: String, codigo: String): Produto

}