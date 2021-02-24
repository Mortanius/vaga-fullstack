package com.wmeds.catalogodeleite.service

import com.wmeds.catalogodeleite.dao.IProdutoDao
import com.wmeds.catalogodeleite.model.Produto
import com.wmeds.catalogodeleite.model.SearchResult
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ProdutoService(
    @Qualifier("postgres") private val dao: IProdutoDao
    ) {

    private val queryTamanhoMinimo = 3

    fun get(codigo: String) = dao.get(codigo)

    fun create(p: Produto) = dao.create(p)

    fun delete(codigo: String) = dao.delete(Produto(codigo, ""))

    private fun isBuscaPorCodigo(query: String): Boolean {
        val regexBuscaPorCodigo = Regex("^\\d{$queryTamanhoMinimo}\\d*$")
        return query.matches(regexBuscaPorCodigo)
    }

    fun search(query: String, offset: Int?, limit: Int?): SearchResult<Produto> {
        if (query.length < queryTamanhoMinimo) {
            throw IllegalArgumentException("A consulta possui tamanho insuficiente")
        }

        if (isBuscaPorCodigo(query)) {
            // Busca por codigo
            return dao.searchByCodigo(query, offset, limit)
        }
        // Busca por nome
        return dao.searchByNome(query, offset, limit)
    }

    fun getIfSatisfiesQuery(query: String, codigo: String): Produto {
        val satisfiesFun: (q: String, c: String) -> Produto =
            if (isBuscaPorCodigo(query)) dao::getIfSatisfiesSearchByCodigo
            else dao::getIfSatisfiesSearchByNome
        return satisfiesFun(query, codigo)
    }

}