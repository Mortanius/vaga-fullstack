import { AfterViewInit, ApplicationRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Produto } from './produto';
import { ProdutoService } from './produto.service';
import { SearchResult } from './searchresult'
import { HttpErrorResponse } from '@angular/common/http';
import { NgbdAlertCloseable } from './alert.component';

interface Search {
  query: string;
  offset: number;
  sort: string[];
  order: string[];  
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, AfterViewInit {
  // title = 'catalogodeleiteapp';

  constructor(private produtoService: ProdutoService, private appRef: ApplicationRef) { }

  // Table
  pageSize: number = 10;
  displayedColumns = ['codigo', 'nome', 'acoes'];
  @ViewChild(MatTable) matTable: MatTable<Produto>;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  pageEvent: PageEvent
  dataSource: MatTableDataSource<Produto>;

  @ViewChild('queryInput') queryInput: ElementRef;

  currentSearch: Search = {query: '', offset: 0, sort: [], order: []};
  produtosSearchResult: SearchResult<Produto> = {items: [], total_count: 0};

  @ViewChild(NgbdAlertCloseable) alertCloseable: NgbdAlertCloseable;
  @ViewChild("mainContainer") mainContainer: ElementRef;

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<Produto>(this.produtosSearchResult.items);
  }

  ngAfterViewInit(): void {
    console.log(this.sort);
    this.dataSource.sort = this.sort;
  }

  private displayMessage(message: any, type: string = 'info') {
    this.alertCloseable.add({type, message: `${message}`});
    const element = this.mainContainer.nativeElement as HTMLElement
    this.appRef.tick();
    const height = this.alertCloseable.getHeight();
    const padding = height > 0 ? height : 10;
    element.style.paddingTop = `${padding}px`;
  }

  private displayErrorMessage(message: any) {
    this.displayMessage(message, 'danger');
  }

  private search(): void {
    this.produtoService.search(this.currentSearch.query,
      this.currentSearch.offset,
      this.pageSize,
      this.currentSearch.sort,
      this.currentSearch.order
    ).subscribe(
      (response: SearchResult<Produto>) => {
        this.produtosSearchResult = response;
        this.dataSource.data = this.produtosSearchResult.items;
      },
      (error: HttpErrorResponse) => {
        this.displayErrorMessage(error.error);
      }
    );
  }

  public searchProdutos(): void {
    this.resetAlert();
    this.resetPaginator();

    this.currentSearch.offset = 0;
    const inputElement = this.queryInput.nativeElement as HTMLInputElement;
    this.currentSearch.query = inputElement.value;
    this.search();
  }

  resetAlert(): void {
    this.alertCloseable.reset();
    const element = this.mainContainer.nativeElement as HTMLElement
    element.style.paddingTop = '10px';
  }

  resetPaginator(): void {
    if (this.paginator == null) {
      return;
    }
    this.paginator.pageIndex = 0;
  }

  makePage(): void {
    this.pageSize = this.paginator.pageSize;
    this.currentSearch.offset = this.paginator.pageIndex * this.paginator.pageSize;
    this.search();
  }

  public onPageChanged(pageEvent: PageEvent): void {
    const previousIndex = pageEvent.previousPageIndex;
    const index = pageEvent.pageIndex;
    if (previousIndex !== index || pageEvent.pageSize != this.pageSize) {
      this.pageSize = pageEvent.pageSize;
      this.currentSearch.offset = index * pageEvent.pageSize;
      this.search();
    }
  }

  private dataSourceAppend(p: Produto): void {
    if (this.dataSource.data.length == this.pageSize) {
      this.dataSource.data.shift();
    }
    this.dataSource.data.push(p);
    this.matTable.renderRows();
  }

  private dataSourceDelete(index: number): void {
    this.dataSource.data.splice(index, 1);
    this.matTable.renderRows();
  }

  public createProduto(): void {
    this.resetAlert();

    const form: HTMLFormElement = document.querySelector("#createProductForm");
    const formData = new FormData(form);
    const p: Produto = {
      codigo: formData.get('codigo').toString(),
      nome: formData.get('nome').toString()
    };
    this.produtoService.create(p).subscribe(
      () => {
        this.displayMessage("Criado com suscesso!", 'success');
        // Adiciona à tabela se satisfaz a consulta
        this.produtoService.getIfSatisfiesQuery(this.currentSearch.query, p.codigo).subscribe(
          (p: Produto) => {
            this.dataSourceAppend(p);
            this.produtosSearchResult.total_count++;
          }
        );
      },
    (error: HttpErrorResponse) => this.displayErrorMessage(error.error)
    );

  }

  replaceDeleted(): void {
    const offset = this.currentSearch.offset + this.pageSize - 1;
    this.produtoService.search(this.currentSearch.query,
      offset,
      1,
      this.currentSearch.sort,
      this.currentSearch.order
    ).subscribe(
      (response: SearchResult<Produto>) => this.dataSourceAppend(response.items[0])
    );
  }

  public deleteProduto(index: number): void {
    this.resetAlert();

    const p = this.dataSource.data[index]
    const confirmation = window.confirm(
      "Deseja remover o produto?\n"+
      `${p.codigo}\n${p.nome}`
    );
    if (confirmation) {
      this.produtoService.delete(p.codigo).subscribe(
        () => {
          this.displayMessage("Removido com sucesso");
          // Remove da tabela
          this.dataSourceDelete(index);
          this.produtosSearchResult.total_count--;
          if (this.dataSource.data.length == 0) {
            // Página anterior
            if (this.paginator.pageIndex > 0) {
              this.paginator.pageIndex--;
              this.makePage();
            }
          } else {
            const nextOffset = this.currentSearch.offset + this.dataSource.data.length
            if (nextOffset < this.produtosSearchResult.total_count) {
              this.replaceDeleted();
            }
          }
        },
        (error: HttpErrorResponse) => {
          this.displayErrorMessage(error.error);
        }
      );
    }
  }

  public sortProdutos(sort: Sort): void {
    // A ideia era de implementar ordenação de várias linhas mas o MatSort só considera uma
    const index = this.currentSearch.sort.findIndex(v => v == sort.active);
    if (index < 0 && sort.direction.length > 0) {
      this.currentSearch.sort = [sort.active];
      this.currentSearch.order = [sort.direction];
    } else if (sort.direction.length == 0) {
      // Remove
      this.currentSearch.sort = [];
      this.currentSearch.order = [];
    } else {
      // Altera
      this.currentSearch.order[index] = sort.direction;
    }
    console.log(this.currentSearch.sort);
    console.log(this.currentSearch.order);
    this.search();
  }
  
}
