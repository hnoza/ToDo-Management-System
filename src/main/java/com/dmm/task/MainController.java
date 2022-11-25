package com.dmm.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {

	// @Autowiredアノテーションを付けると、Spring Bootが自動でインスタンスをInjectします。
	@Autowired
	private TasksRepository repo;

	private static LocalDate displayMonth;
	
	/**
	 * カレンダー表示.
	 * 
	 * @param model モデル
	 * @param user ユーザ情報
	 * @return 遷移先
	 */
	@GetMapping("/main")
	public String tasks(Model model, @AuthenticationPrincipal AccountUserDetails user, @RequestParam(name = "date", required = false) String date) {

		List<List<LocalDate>> matrixCal = new ArrayList<>();	// 月のLocalDateのリスト
		LocalDate currentDate = null; // 追加対象のLocalDate
		
		// 表示している月情報を保存
		// loginFormから遷移した場合はdateはnullになる
		if (date == null) {
			displayMonth = LocalDate.now();
		} else {
			displayMonth = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		
		// 先月、来月情報をテンプレートに渡す
		model.addAttribute("prev", displayMonth.plusMonths(-1));
		model.addAttribute("next", displayMonth.plusMonths(1));
		
		// -------------------------
		// 1週目のListを作成
		// -------------------------
		// 月初のLocalDateを取得
		LocalDate firstDate = displayMonth.withDayOfMonth(1);
		
		// カレンダータイトルをテンプレートに渡す
		model.addAttribute("month", displayMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
		
		// 月初の曜日からカレンダーに表示する前月のLocalDateを取得
		DayOfWeek youbi = firstDate.getDayOfWeek();
		LocalDate lastmonthday = firstDate.plusDays(-youbi.getValue());
		
		// 1週目を追加
		matrixCal.add(createOneWeekList(lastmonthday));
		
		// 1週目の最終日のcurrentDateを取得、日付を1つ進める
		currentDate = matrixCal.get(0).get(6);
		currentDate = currentDate.plusDays(1);
		
		// -------------------------
		// 2週目以降を追加
		// -------------------------	
		while (currentDate.isBefore(displayMonth.with(TemporalAdjusters.lastDayOfMonth())) ) {

			matrixCal.add(createOneWeekList(currentDate));
			currentDate = currentDate.plusDays(7);
		}
		
		
		// カレンダーをテンプレートに渡す。
		model.addAttribute("matrix", matrixCal);
		
		
		// -------------------------
		// TaskListをDBから取得
		// -------------------------
		List<Tasks> taskList = new ArrayList<>();
		
		// ユーザ権限によって表示するタスクを切り替える
		boolean isAdmin = false;
		
		Collection<? extends GrantedAuthority> authList = user.getAuthorities();
		
		for ( GrantedAuthority auth : authList ) {
			if ("ROLE_ADMIN".equals(auth.getAuthority())) {
				isAdmin = true;
				break;
			}
		}
		
		if ( isAdmin ) { // admin権限の場合はすべてのタスクを表示
			taskList = repo.findByDateBetween(
					displayMonth.with(TemporalAdjusters.firstDayOfMonth()),
					displayMonth.with(TemporalAdjusters.lastDayOfMonth()));
		} else {
			taskList = repo.findByDateBetween(
					displayMonth.with(TemporalAdjusters.firstDayOfMonth()),
					displayMonth.with(TemporalAdjusters.lastDayOfMonth()),
					user.getName());
		}
		
	    // テンプレートに渡すタスクマップ作成
	    MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
	    
	    for (Tasks task : taskList) {
	    	tasks.add(task.getDate(), task);
	    }
	    
	    // タスクマップをテンプレートに渡す
	    model.addAttribute("tasks", tasks);
	    
		return "main"; // main.html
	}

	/**
	 * startDateから1週間分のカレンダーを作成する
	 * 
	 * @param startDate 週の開始日
	 * @return List<LocalDate> 1週間分のカレンダー
	 */
	private List<LocalDate> createOneWeekList(LocalDate startDate) {

		List<LocalDate> oneWeekTaskList = new ArrayList<>();

		for (int plusDay = 0; plusDay < 7; plusDay++) {
			oneWeekTaskList.add(startDate.plusDays(plusDay));
		}

		return oneWeekTaskList;
	}

}
