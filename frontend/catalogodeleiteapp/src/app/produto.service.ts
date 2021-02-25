import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http'
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Produto } from './produto';
import { SearchResult } from './searchresult';

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private apiServerUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) { } 

  public get(codigo: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.apiServerUrl}/produtos/${codigo}`);
  }

  public create(produto: Produto): Observable<Produto> {
    return this.http.post<Produto>(`${this.apiServerUrl}/produtos`, produto);
  }

  public delete(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.apiServerUrl}/produtos/${codigo}`);
  }

  public search(
    query: string,
    offset: number,
    limit: number,
    sort: string[] = [],
    order: string[] = []
  ): Observable<SearchResult<Produto>> {
    const sortStr = sort.length == 0 ? "" :
      `&sort=${sort.join('&sort=')}`
    const orderStr = order.length == 0 ? "" :
      `&order=${order.join('&order=')}`
    const uri = `${this.apiServerUrl}/produtos/search?query=${query}&offset=${offset}&limit=${limit}${sortStr}${orderStr}`
    return this.http.get<SearchResult<Produto>>(uri);
  }

  public getIfSatisfiesQuery(query: string, codigo: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.apiServerUrl}/produtos/${codigo}/ifSatisfies?query=${query}`);
  }
}
