import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-acesso-negado',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './acesso-negado.html',
  styleUrl: './acesso-negado.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AcessoNegado {}
