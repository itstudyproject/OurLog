package com.example.ourLog.controller;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("question")
@Log4j2
@RequiredArgsConstructor
public class QuestionController {
  private final QuestionService questionService;

  @GetMapping({"", "/", "/list"})
  public String list(PageRequestDTO pageRequestDTO, Model model) {
    model.addAttribute("result", questionService.getList(pageRequestDTO));
    return "/question/list";
  }

  @GetMapping("/register")
  public void register() {  }

  @PostMapping("/register")
  public String registerQuestion(QuestionDTO questionDTO, RedirectAttributes ra) {
    Long questionId = questionService.register(questionDTO);
    ra.addFlashAttribute("msg", questionId + "문의사항이 등록");
    return "redirect:/question/list";
  }

  @GetMapping({"/read", "/modify"})
  public void read(Long questionId, PageRequestDTO pageRequestDTO, Model model) {
    QuestionDTO questionDTO = questionService.get(questionId);
    model.addAttribute("questionDTO", questionDTO);
  }

  @PostMapping("/modify")
  public String modify(QuestionDTO questionDTO,
                       PageRequestDTO pageRequestDTO, RedirectAttributes ra) {
    questionService.modify(questionDTO);
    ra.addFlashAttribute("msg", questionDTO.getQuestionId() + "문의사항이 수정");
    ra.addAttribute("questionId", questionDTO.getQuestionId());
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/question/read";
  }

  @PostMapping("/remove")
  public String remove(QuestionDTO questionDTO,
                       PageRequestDTO pageRequestDTO, RedirectAttributes ra) {
    questionService.removeWithAnswer(questionDTO.getQuestionId());

    if (questionService.getList(pageRequestDTO).getDtoList().size() == 0
            && pageRequestDTO.getPage() != 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }
    ra.addFlashAttribute("msg", questionDTO.getQuestionId() + "문의사항이 삭제");
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/question/list";
  }

}
