package com.wmeds.catalogodeleite.controller

import com.wmeds.catalogodeleite.model.Produto
import com.wmeds.catalogodeleite.model.SearchResult
import com.wmeds.catalogodeleite.service.ProdutoService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:4200", "http://localhost:8000"])
@RestController
@RequestMapping("/api/produtos")
class ProdutoController(private val service: ProdutoService) {

    @ExceptionHandler(
        IllegalArgumentException::class,
        DataIntegrityViolationException::class
    )
    fun handleBadRequest(e: Exception): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @GetMapping("/{codigo}")
    fun get(@PathVariable codigo: String) = service.get(codigo)

    @PostMapping
    fun create(@RequestBody p: Produto) = service.create(p)

    @DeleteMapping("/{codigo}")
    fun delete(@PathVariable codigo: String) = service.delete(codigo)

    @PutMapping("/{codigo}")
    fun update(@PathVariable codigo: String, @RequestBody p: Produto) = service.update(codigo, p)

    @GetMapping("/search")
    fun search(
        @RequestParam query: String,
        @RequestParam offset: Int?,
        @RequestParam limit: Int?,
        @RequestParam sort: Array<String>?,
        @RequestParam order: Array<String>?
    ): SearchResult<Produto> =
        service.search(query, offset, limit, sort, order)

    @GetMapping("/{codigo}/ifSatisfies")
    fun getIfSatisfiesQuery(@RequestParam query: String,
                            @PathVariable codigo: String
    ): Produto =
        service.getIfSatisfiesQuery(query, codigo)

}