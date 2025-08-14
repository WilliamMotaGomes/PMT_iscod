import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';

type Role = 'ADMIN'|'MEMBER'|'OBSERVER';
type HistoryField = 'name' | 'description' | 'dueDate' | 'completedAt' | 'priority' | 'status' | 'assignedTo';

@Component({
  selector: 'app-task-detail',
  templateUrl: `task-detail.component.html`
})
export class TaskDetailComponent
{
  task: any;
  members: any[] = [];
  assigneeId = 0;
  edit = false;
  history: any[] = [];
  role: Role | null = null;

  form: any = { name: '', description: '', dueDate: '', priority: 'MEDIUM', completedAt: '' };

  constructor(private api: ApiService, private route: ActivatedRoute, private router: Router, public session: SessionService)
  {
    this.load();
  }

  /* Verifie si l'utilisateur peut editer la tâche  */
  get canEdit()
  {
      return this.role === 'ADMIN' || this.role === 'MEMBER';
  }

  /* Verifie si l'utilisateur peut attribuer la tâche à un autre utilsateur du projet */
  get canAssign()
  {
      return this.role === 'ADMIN' || this.role === 'MEMBER';
  }

  /* Recupere les infos de la tâche ouverte*/
  async load()
  {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.task = await this.api.task(this.session.userId!, id);
    this.form =
    {
      name: this.task.name,
      description: this.task.description,
      dueDate: this.toDateInput(this.task.dueDate),
      priority: this.task.priority || 'MEDIUM',
      completedAt: this.task.completedAt ? this.toDateInput(this.task.completedAt) : ''
    };
    const projectId = this.task.project?.id || this.task.projectId;
    if (projectId)
    {
      const me = await this.api.me(projectId);
      this.role = (me && me.role) ? me.role : null;
      this.members = await this.api.members(projectId);
    }

    this.history = await this.api.taskHistory(id);
  }

  /* Formatte la date */
  fmtDate(v:any): string
  {
    if (!v)
        return '';

    try
    {
      const d = new Date(v);
      if (!isNaN(d.getTime())) return d.toLocaleDateString();
    }
    catch {}
    if (typeof v === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(v))
        return v;

    return '';
  }

  /* Converti un string vers une date  */
  toDateInput(v:any): string
  {
    if (!v)
        return '';

    try
    {
      const d = new Date(v);
      if (!isNaN(d.getTime()))
        return d.toISOString().slice(0,10);
    }
    catch {}
    if (typeof v === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(v))
        return v;

    return '';
  }

  /* Converti une date vers une date iso (le temps a 00h00)  */
  private completedAtToISO(dateOnly: string): string
  {
      return dateOnly ? `${dateOnly}T00:00:00.000Z` : '';
  }

  /* Map pour afficher les champs qui ont ete modifiés dans l'historique */
  private labelOf(field: string): string
  {
    const map: any =
    {
      name: 'titre', description: 'description', dueDate: 'échéance',
      priority: 'priorité', status: 'statut', completedAt: 'date de fin',
      assignedTo: 'assignée à', assigneeId: 'assignée à', assignee: 'assignée à'
    };
    return map[field] || field;
  }

  /* Transforme une valeur (any) en un string (si c'est une date ISO on recupere juste la date sans l'heure) */
  private stringVal(v: any): string
  {
    if (v === null || v === undefined || v === '')
        return '—';

    if (typeof v === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(v))
        return v.slice(0,10);

    return String(v);
  }

  /* Ecrit la phrase affiche dans l'historique de la tache */
  renderDiff(h: any)
  {
    const field = h?.field || h?.changedField;
    const oldV = h?.oldValue;
    const newV = h?.newValue;

    if (field === 'assignedTo' || field === 'assigneeId' || field === 'assignee')
    {
      return `${this.labelOf('assignedTo')} : ${this.userLabel(oldV)} -> ${this.userLabel(newV)}`;
    }
    if (field === 'dueDate' || field === 'completedAt')
    {
      return `${this.labelOf(field)} : ${this.fmtDate(oldV) || '—'} -> ${this.fmtDate(newV) || '—'}`;
    }
    if (field)
    {
      return `${this.labelOf(field)} : ${this.stringVal(oldV)} -> ${this.stringVal(newV)}`;
    }

    return h?.text || 'maj';
  }

  /* Retourne la date et l'heure lors de la modification d'une tâche */
  renderWhen(h:any)
  {
    const raw = h?.at ?? h?.timestamp;
    if (!raw)
        return '—';

    const d = new Date(raw);

    return isNaN(d.getTime()) ? String(raw) : d.toLocaleString();
  }

  /* Active le mode pour modifier une tâche */
  toggleEdit()
  {
      this.edit = !this.edit;
  }

  /* Annule les modifications si elles ne sont pas sauvegardées */
  cancel()
  {
    this.edit = false;
    this.form =
    {
      name: this.task.name,
      description: this.task.description,
      dueDate: this.toDateInput(this.task.dueDate),
      priority: this.task.priority,
      completedAt: this.task.completedAt ? this.toDateInput(this.task.completedAt) : ''
    };
  }

 /* Renvoie les elements de la tâche qui ont ete modifiés */
 private buildDiffs(prev: any, nextPartial: any)
 {
   const diffs: Array<{
     action: 'UPDATED' | 'STATUS' | 'ASSIGNEE';
     field?: HistoryField;
     oldValue?: string | null;
     newValue?: string | null;
   }> = [];

   const toStr = (v: any): string | null =>
     v === undefined || v === null || v === '' ? null :
     (typeof v === 'string' ? v :
      (v instanceof Date ? v.toISOString().slice(0,10) : String(v)));

   const cmp = (a: any, b: any) => (a ?? null) !== (b ?? null);

   if ('name' in nextPartial && cmp(prev.name, nextPartial.name))
     diffs.push({ action:'UPDATED', field:'name', oldValue: toStr(prev.name), newValue: toStr(nextPartial.name) });

   if ('description' in nextPartial && cmp(prev.description, nextPartial.description))
     diffs.push({ action:'UPDATED', field:'description', oldValue: toStr(prev.description), newValue: toStr(nextPartial.description) });

   if ('dueDate' in nextPartial && cmp(this.toDateInput(prev.dueDate), nextPartial.dueDate))
     diffs.push({ action:'UPDATED', field:'dueDate', oldValue: this.toDateInput(prev.dueDate) || null, newValue: nextPartial.dueDate || null });

   if ('priority' in nextPartial && cmp(prev.priority, nextPartial.priority))
     diffs.push({ action:'UPDATED', field:'priority', oldValue: toStr(prev.priority), newValue: toStr(nextPartial.priority) });

   if ('completedAt' in nextPartial && cmp(this.toDateInput(prev.completedAt), this.toDateInput(nextPartial.completedAt)))
     diffs.push({ action:'UPDATED', field:'completedAt', oldValue: this.toDateInput(prev.completedAt) || null, newValue: this.toDateInput(nextPartial.completedAt) || null });

   if ('status' in nextPartial && cmp(prev.status, nextPartial.status))
     diffs.push({ action:'STATUS', field:'status', oldValue: toStr(prev.status), newValue: toStr(nextPartial.status) });

   return diffs;
 }

  /* Affiche dans l'historique les differents changement qui ont eu lieu sur la tâche */
  private async postHistory(taskId: number,diffs: Array<{ action:'UPDATED'|'STATUS'|'ASSIGNEE'; field?: HistoryField; oldValue?: string|null; newValue?: string|null }>)
  {
    if (!diffs.length)
        return;

    const authorId = this.session.userId!;

    try
    {
      await Promise.all(diffs.map(d =>
        this.api.addTaskHistory(taskId, {
          authorId,
          action: d.action,
          field: d.field,
          oldValue: d.oldValue ?? null,
          newValue: d.newValue ?? null
        })
      ));
    }
    catch (e)
    {
      console.warn('history post failed', e);
    }
  }

  /* Sauvegarde les modifications */
  async save()
  {
    if (!this.canEdit)
        return;

    const before = {...this.task};
    const payload: any =
    {
      name: this.form.name,
      description: this.form.description,
      dueDate: this.form.dueDate || null,
      priority: this.form.priority
    };

    if (this.form.completedAt)
        payload.completedAt = this.completedAtToISO(this.form.completedAt);
    else payload.completedAt = null;

    const diffs = this.buildDiffs(before, payload);
    await this.api.updateTask(this.session.userId!, this.task.id, payload);
    await this.postHistory(this.task.id, diffs);
    await this.load();
    this.edit = false;
  }

  /* Change le statut de la tâche */
  async setStatus(s:'TODO'|'IN_PROGRESS'|'DONE')
  {
    if (!this.canEdit)
        return;

    const before = {...this.task};
    const payload:any = { status: s };

    if (s==='DONE')
    {
      const today = this.toDateInput(new Date().toISOString());
      payload.completedAt = this.completedAtToISO(this.form.completedAt || today);
    }

    const diffs = this.buildDiffs(before, payload);
    await this.api.updateTask(this.session.userId!, this.task.id, payload);
    await this.postHistory(this.task.id, diffs);
    await this.load();
  }

  /* Assigne la tâche à un utilisateur */
  async assign()
  {
    if (!this.canAssign || !this.assigneeId)
        return;

    const beforeId = this.task.assignedTo?.id || null;

    if (beforeId === this.assigneeId)
        return;

    await this.api.assignTask(this.task.id, this.assigneeId);
    const beforeName = this.task.assignedTo?.username || (beforeId ? `User#${beforeId}` : '—');
    const afterMember = this.members.find(m => m.user?.id === this.assigneeId);
    const afterName = afterMember?.user?.username || `User#${this.assigneeId}`;

    await this.postHistory(this.task.id, [{
      action:'ASSIGNEE',
      field:'assignedTo',
      oldValue: beforeName,
      newValue: afterName
    }]);

    await this.load();
  }

  /* Retour au dashboard */
  back()
  {
      this.router.navigate(['/dashboard']);
  }

  /* Retourne le nom d'un utilisateur depuis differents objet */
  private userLabel(val: any): string
  {
    if (val === null || val === undefined || val === '')
        return '—';

    if (typeof val === 'object')
    {
      if (val.username)
        return val.username;
      if (val.user?.username)
        return val.user.username;
    }

    const toId = (x: any): number | null =>
    {
      if (typeof x === 'number')
        return x;

      const s = String(x);
      const m = s.match(/User#?(\d+)/i);

      if (m)
        return Number(m[1]);
      if (/^\d+$/.test(s))
        return Number(s);

      const mm = this.members.find(u =>
        u.user?.username === s || u.user?.email === s
      );

      return mm?.user?.id ?? null;
    };
    const id = toId(val);
    if (id != null)
    {
      const m = this.members.find(u => u.user?.id === id);
      return m?.user?.username || `User#${id}`;
    }

    return String(val);
  }
}
