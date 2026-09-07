import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { OrcamentoService } from '../../../core/services/orcamento.service';
import { ProdutoService } from '../../../core/services/produto.service';
import { ServicoService } from '../../../core/services/servico.service';
import {
  OrcamentoHistorico,
  OrcamentoItemResponse,
  OrcamentoResponse,
  StatusOrcamento,
  TRANSICOES_STATUS_ORCAMENTO
} from '../../../core/models/orcamento.model';
import { ProdutoResponse } from '../../../core/models/produto.model';
import { ServicoResponse } from '../../../core/models/servico.model';
import { extractErrorMessage } from '../../../core/utils/api-error.util';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Loading } from '../../../shared/components/loading/loading';

const STATUS_LABEL: Record<StatusOrcamento, string> = {
  RASCUNHO: 'Rascunho',
  ENVIADO: 'Enviado',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
  CANCELADO: 'Cancelado'
};

@Component({
  selector: 'app-orcamento-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatSelectModule,
    ErrorMessage,
    Loading
  ],
  templateUrl: './orcamento-detail.html',
  styleUrl: './orcamento-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrcamentoDetail implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly servicoService = inject(ServicoService);
  private readonly produtoService = inject(ProdutoService);
  private readonly authService = inject(AuthService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly snackBar = inject(MatSnackBar);

  readonly statusLabel = STATUS_LABEL;

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly orcamento = signal<OrcamentoResponse | null>(null);
  readonly itens = signal<OrcamentoItemResponse[]>([]);
  readonly historico = signal<OrcamentoHistorico[]>([]);
  readonly servicos = signal<ServicoResponse[]>([]);
  readonly produtos = signal<ProdutoResponse[]>([]);

  readonly alterandoStatus = signal(false);
  readonly adicionandoServico = signal(false);
  readonly adicionandoProduto = signal(false);
  readonly editandoItemId = signal<number | null>(null);
  readonly salvandoItem = signal(false);

  readonly podeGerenciar = computed(() => this.authService.hasRole('ADMIN', 'ATENDENTE'));
  readonly emRascunho = computed(() => this.orcamento()?.status === 'RASCUNHO');
  readonly podeEditarItens = computed(() => this.podeGerenciar() && this.emRascunho());
  readonly proximosStatus = computed(() => {
    const orcamento = this.orcamento();
    if (!orcamento) {
      return [];
    }
    const proximos = TRANSICOES_STATUS_ORCAMENTO[orcamento.status];
    return orcamento.status === 'RASCUNHO' && this.itens().length === 0
      ? proximos.filter((s) => s !== 'ENVIADO')
      : proximos;
  });

  readonly itemServicoForm = this.fb.group({
    servicoId: this.fb.control<number | null>(null, [Validators.required]),
    quantidade: this.fb.control<number | null>(1, [Validators.required, Validators.min(1)]),
    valorUnitario: this.fb.control<number | null>(null)
  });

  readonly itemProdutoForm = this.fb.group({
    produtoId: this.fb.control<number | null>(null, [Validators.required]),
    quantidade: this.fb.control<number | null>(1, [Validators.required, Validators.min(1)]),
    valorUnitario: this.fb.control<number | null>(null)
  });

  readonly itemEditForm = this.fb.group({
    quantidade: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
    valorUnitario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)])
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.servicoService.listarAtivos().subscribe((servicos) => this.servicos.set(servicos));
    this.produtoService.listarAtivos().subscribe((produtos) => this.produtos.set(produtos));

    this.carregar(id);
  }

  alterarStatus(novoStatus: StatusOrcamento): void {
    const orcamento = this.orcamento();
    if (!orcamento) {
      return;
    }

    this.alterandoStatus.set(true);

    this.orcamentoService.atualizarStatus(orcamento.id, novoStatus).subscribe({
      next: (atualizado) => {
        this.orcamento.set(atualizado);
        this.alterandoStatus.set(false);
        this.carregarHistorico(orcamento.id);
      },
      error: (error: unknown) => {
        this.alterandoStatus.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível alterar o status.'), 'Ok', {
          duration: 4000
        });
      }
    });
  }

  adicionarServico(): void {
    const orcamento = this.orcamento();
    if (!orcamento || this.itemServicoForm.invalid) {
      this.itemServicoForm.markAllAsTouched();
      return;
    }

    this.adicionandoServico.set(true);
    const { servicoId, quantidade, valorUnitario } = this.itemServicoForm.getRawValue();

    this.orcamentoService
      .adicionarItemServico(orcamento.id, {
        servicoId: servicoId!,
        quantidade: quantidade!,
        valorUnitario: valorUnitario ?? undefined
      })
      .subscribe({
        next: () => {
          this.adicionandoServico.set(false);
          this.itemServicoForm.reset({ servicoId: null, quantidade: 1, valorUnitario: null });
          this.recarregarItensETotal(orcamento.id);
        },
        error: (error: unknown) => {
          this.adicionandoServico.set(false);
          this.snackBar.open(extractErrorMessage(error, 'Não foi possível adicionar o serviço.'), 'Ok', {
            duration: 4000
          });
        }
      });
  }

  adicionarProduto(): void {
    const orcamento = this.orcamento();
    if (!orcamento || this.itemProdutoForm.invalid) {
      this.itemProdutoForm.markAllAsTouched();
      return;
    }

    this.adicionandoProduto.set(true);
    const { produtoId, quantidade, valorUnitario } = this.itemProdutoForm.getRawValue();

    this.orcamentoService
      .adicionarItemProduto(orcamento.id, {
        produtoId: produtoId!,
        quantidade: quantidade!,
        valorUnitario: valorUnitario ?? undefined
      })
      .subscribe({
        next: () => {
          this.adicionandoProduto.set(false);
          this.itemProdutoForm.reset({ produtoId: null, quantidade: 1, valorUnitario: null });
          this.recarregarItensETotal(orcamento.id);
        },
        error: (error: unknown) => {
          this.adicionandoProduto.set(false);
          this.snackBar.open(extractErrorMessage(error, 'Não foi possível adicionar o produto.'), 'Ok', {
            duration: 4000
          });
        }
      });
  }

  iniciarEdicaoItem(item: OrcamentoItemResponse): void {
    this.editandoItemId.set(item.id);
    this.itemEditForm.setValue({ quantidade: item.quantidade, valorUnitario: item.valorUnitario });
  }

  cancelarEdicaoItem(): void {
    this.editandoItemId.set(null);
  }

  salvarEdicaoItem(item: OrcamentoItemResponse): void {
    const orcamento = this.orcamento();
    if (!orcamento || this.itemEditForm.invalid) {
      this.itemEditForm.markAllAsTouched();
      return;
    }

    this.salvandoItem.set(true);
    const { quantidade, valorUnitario } = this.itemEditForm.getRawValue();

    this.orcamentoService.atualizarItem(orcamento.id, item.id, { quantidade: quantidade!, valorUnitario: valorUnitario! }).subscribe({
      next: () => {
        this.salvandoItem.set(false);
        this.editandoItemId.set(null);
        this.recarregarItensETotal(orcamento.id);
      },
      error: (error: unknown) => {
        this.salvandoItem.set(false);
        this.snackBar.open(extractErrorMessage(error, 'Não foi possível atualizar o item.'), 'Ok', { duration: 4000 });
      }
    });
  }

  removerItem(item: OrcamentoItemResponse): void {
    const orcamento = this.orcamento();
    if (!orcamento) {
      return;
    }

    this.confirmDialog
      .confirm({
        title: 'Remover item',
        message: `Remover "${item.descricao}" do orçamento?`,
        confirmText: 'Remover',
        destructive: true
      })
      .subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }

        this.orcamentoService.removerItem(orcamento.id, item.id).subscribe({
          next: () => this.recarregarItensETotal(orcamento.id),
          error: (error: unknown) => {
            this.snackBar.open(extractErrorMessage(error, 'Não foi possível remover o item.'), 'Ok', {
              duration: 4000
            });
          }
        });
      });
  }

  private carregar(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      orcamento: this.orcamentoService.buscarPorId(id),
      itens: this.orcamentoService.listarItens(id),
      historico: this.orcamentoService.listarHistorico(id)
    }).subscribe({
      next: ({ orcamento, itens, historico }) => {
        this.orcamento.set(orcamento);
        this.itens.set(itens);
        this.historico.set(historico);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(extractErrorMessage(error, 'Não foi possível carregar o orçamento.'));
        this.loading.set(false);
      }
    });
  }

  private recarregarItensETotal(id: number): void {
    forkJoin({
      orcamento: this.orcamentoService.buscarPorId(id),
      itens: this.orcamentoService.listarItens(id)
    }).subscribe(({ orcamento, itens }) => {
      this.orcamento.set(orcamento);
      this.itens.set(itens);
    });
  }

  private carregarHistorico(id: number): void {
    this.orcamentoService.listarHistorico(id).subscribe((historico) => this.historico.set(historico));
  }
}
