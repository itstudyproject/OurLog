package com.example.ourLog.controller;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.QnADTO;
import com.example.ourLog.service.QnAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("qna")
@Log4j2
@RequiredArgsConstructor
public class QnAController {
  private final QnAService qnAService;

  @GetMapping({"", "/", "/list"})
  public String list(PageRequestDTO pageRequestDTO, Model model) {
    model.addAttribute("result", qnAService.getList(pageRequestDTO));
    return "/qna/list";
  }

  @GetMapping("/register")
  public void register() {  }

  @PostMapping("/register")
  public String registerQnA(QnADTO qnADTO, RedirectAttributes ra) {
    Long bno = qnAService.register(qnADTO);
    ra.addFlashAttribute("msg", bno + "문의사항이 등록");
    return "redirect:/qna/list";
  }

  @GetMapping({"/read", "/modify"})
  public void read(Long qnaId, PageRequestDTO pageRequestDTO, Model model) {
    QnADTO qnADTO = qnAService.get(qnaId);
    model.addAttribute("qnADTO", qnADTO);
  }
  @PostMapping("/modify")
  public String modify(QnADTO qnADTO,
                       PageRequestDTO pageRequestDTO, RedirectAttributes ra) {
    qnAService.modify(qnADTO);
    ra.addFlashAttribute("msg", qnADTO.getQnaId() + "문의사항이 수정");
    ra.addAttribute("qnaId", qnADTO.getQnaId());
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/qna/read";
  }

  @PostMapping("/remove")
  public String remove(QnADTO qnADTO,
                       PageRequestDTO pageRequestDTO, RedirectAttributes ra) {
    qnAService.removeWithReplies(qnADTO.getQnaId());

    if (qnAService.getList(pageRequestDTO).getDtoList().size() == 0
            && pageRequestDTO.getPage() != 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }
    ra.addFlashAttribute("msg", qnADTO.getQnaId() + "문의사항이 삭제");
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/qna/list";
  }

}
