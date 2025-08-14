import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { DashboardComponent } from './dashboard.component';
import { ApiService } from '../services/api.service';
import { SessionService } from '../services/session.service';

describe('DashboardComponent (branches heavy)', () =>
{
  let comp: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let api: jasmine.SpyObj<ApiService>;
  let router: Router;

  const sessionMock = { userId: 1 } as unknown as SessionService;

  beforeEach(async () =>
  {
    api = jasmine.createSpyObj<ApiService>('ApiService', [
      'projects',
      'createProject',
      'createTask',
      'tasks',
      'me',
      'members',
      'invite',
      'setRole',
    ]);

    api.projects.and.returnValue(Promise.resolve([{ id: 1, name: 'P1' }]));
    api.tasks.and.returnValue(Promise.resolve([]));
    api.createTask.and.returnValue(Promise.resolve({ id: 101 }));
    api.me.and.returnValue(Promise.resolve({ role: 'ADMIN' }));
    api.members.and.returnValue(Promise.resolve([]));
    api.createProject.and.returnValue(Promise.resolve({ id: 5 }));
    api.invite.and.returnValue(Promise.resolve({}));
    api.setRole.and.returnValue(Promise.resolve({}));

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [FormsModule, RouterTestingModule],
      providers: [
        { provide: ApiService, useValue: api },
        { provide: SessionService, useValue: sessionMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    comp = fixture.componentInstance;
    router = TestBed.inject(Router);

    localStorage.removeItem('currentProjectId');

    fixture.detectChanges();
  });

  afterEach(() =>
  {
    localStorage.removeItem('currentProjectId');
  });

  it('canAdmin / canEdit should reflect role', () =>
  {
    (comp as any).role = 'ADMIN';
    expect(comp.canAdmin).toBeTrue();
    expect(comp.canEdit).toBeTrue();

    (comp as any).role = 'MEMBER';
    expect(comp.canAdmin).toBeFalse();
    expect(comp.canEdit).toBeTrue();

    (comp as any).role = 'OBSERVER';
    expect(comp.canAdmin).toBeFalse();
    expect(comp.canEdit).toBeFalse();

    (comp as any).role = null;
    expect(comp.canAdmin).toBeFalse();
    expect(comp.canEdit).toBeFalse();
  });

  it('loadTasks early-return when no projectId', async () =>
  {
    comp.projectId = null;
    await comp.loadTasks();

    expect(api.tasks).not.toHaveBeenCalled();
  });

  it('loadTasks loads role if missing, and respects filterStatus variants', async () =>
  {
    comp.projectId = 1;
    comp['role'] = null;
    api.me.calls.reset();
    api.tasks.calls.reset();

    comp.filterStatus = '';
    await comp.loadTasks();

    expect(api.me).toHaveBeenCalledWith(1);
    expect(api.tasks).toHaveBeenCalledWith(1, 1, undefined);

    api.me.calls.reset();
    api.tasks.calls.reset();
    comp.filterStatus = 'TODO';
    await comp.loadTasks();

    expect(api.me).not.toHaveBeenCalled();
    expect(api.tasks).toHaveBeenCalledWith(1, 1, 'TODO');
  });

  it('onSelectProject stores id and triggers loaders', () =>
  {
    const roleSpy = spyOn(comp, 'loadRole').and.returnValue(Promise.resolve());
    const membersSpy = spyOn(comp, 'loadMembers').and.returnValue(Promise.resolve());
    const tasksSpy = spyOn(comp, 'loadTasks').and.returnValue(Promise.resolve());

    comp.onSelectProject(7);

    expect(localStorage.getItem('currentProjectId')).toBe('7');
    expect(roleSpy).toHaveBeenCalled();
    expect(membersSpy).toHaveBeenCalled();
    expect(tasksSpy).toHaveBeenCalled();
  });

  it('createProject calls API then reloads and selects new project', async () =>
  {
    const projectsSpy = spyOn(comp, 'loadProjects').and.callThrough();
    const selectSpy = spyOn(comp, 'onSelectProject').and.callThrough();
    comp.pname = 'N';
    comp.pdesc = 'D';
    comp.pstart = '2025-08-10';

    await comp.createProject();

    expect(api.createProject).toHaveBeenCalledWith(1, 'N', 'D', '2025-08-10');
    expect(projectsSpy).toHaveBeenCalled();
    expect(selectSpy).toHaveBeenCalledWith(5);
  });

  it('createTask respects guards: not called if no projectId or cannot edit; called when allowed', async () =>
  {
    comp.projectId = null;
    comp['role'] = 'ADMIN';
    await comp.createTask();

    expect(api.createTask).not.toHaveBeenCalled();

    comp.projectId = 1;
    comp['role'] = 'OBSERVER';
    await comp.createTask();

    expect(api.createTask).not.toHaveBeenCalled();

    comp['role'] = 'MEMBER';
    comp.tname = 'T';
    comp.tdesc = 'D';
    comp.tdue = '2025-08-10';
    comp.tprio = 'HIGH';
    await comp.createTask();

    expect(api.createTask).toHaveBeenCalledWith(1, 1, {
      name: 'T',
      description: 'D',
      dueDate: '2025-08-10',
      priority: 'HIGH',
    });
  });

  it('invite respects guards: requires projectId, email and ADMIN', async () =>
  {
    const membersSpy = spyOn(comp, 'loadMembers').and.returnValue(Promise.resolve());
    const projectsSpy = spyOn(comp, 'loadProjects').and.returnValue(Promise.resolve());

    comp.projectId = null;
    comp.inviteEmail = 'x@y.z';
    comp['role'] = 'ADMIN';
    await comp.invite();

    expect(api.invite).not.toHaveBeenCalled();

    comp.projectId = 3;
    comp.inviteEmail = '';
    comp['role'] = 'ADMIN';
    await comp.invite();

    expect(api.invite).not.toHaveBeenCalled();

    comp.projectId = 3;
    comp.inviteEmail = 'x@y.z';
    comp['role'] = 'MEMBER';
    await comp.invite();

    expect(api.invite).not.toHaveBeenCalled();

    comp['role'] = 'ADMIN';
    await comp.invite();

    expect(api.invite).toHaveBeenCalledWith(3, 'x@y.z');
    expect(membersSpy).toHaveBeenCalled();
    expect(projectsSpy).toHaveBeenCalled();
    expect(comp.inviteEmail).toBe('');
  });

  it('updateRole respects guards: only ADMIN can call', async () =>
  {
    const membersSpy = spyOn(comp, 'loadMembers').and.returnValue(Promise.resolve());

    comp.projectId = null;
    comp['role'] = 'ADMIN';
    await comp.updateRole(9, 'MEMBER');

    expect(api.setRole).not.toHaveBeenCalled();

    comp.projectId = 2;
    comp['role'] = 'MEMBER';
    await comp.updateRole(9, 'OBSERVER');

    expect(api.setRole).not.toHaveBeenCalled();

    comp['role'] = 'ADMIN';
    await comp.updateRole(9, 'OBSERVER');

    expect(api.setRole).toHaveBeenCalledWith(2, 9, 'OBSERVER');
    expect(membersSpy).toHaveBeenCalled();
  });

  it('openTask navigates', () =>
  {
    const navSpy = spyOn(router, 'navigate');
    comp.openTask(123);

    expect(navSpy).toHaveBeenCalledWith(['/task', 123]);
  });

  it('ngOnInit selects saved project if available, otherwise the first', async () =>
   {
    const selSpy = spyOn(comp, 'onSelectProject').and.callThrough();

    localStorage.setItem('currentProjectId', '99');
    const c1 = TestBed.createComponent(DashboardComponent).componentInstance;
    await c1.ngOnInit();

    expect(selSpy).toHaveBeenCalledWith(99);

    localStorage.removeItem('currentProjectId');
    const c2 = TestBed.createComponent(DashboardComponent).componentInstance;

    api.projects.and.returnValue(Promise.resolve([{ id: 42, name: 'P42' }]));
    await c2.ngOnInit();

    expect(c2.projectId).toBe(42);
  });
});